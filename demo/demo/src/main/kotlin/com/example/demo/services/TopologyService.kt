package com.example.demo.services

import com.example.demo.domain.SimulationOutput
import com.example.demo.domain.TopologyPlanner
import com.example.demo.domain.toOutput
import com.example.demo.domain.toResponse
import com.example.demo.pipeline.PerformanceMetrics
import com.example.demo.pipeline.ScheduledTopologyOutput
import com.example.demo.pipeline.TopologyGroupRequest
import com.example.demo.pipeline.TopologyRequest
import com.example.demo.pipeline.TopologySaveRequest
import com.example.demo.pipeline.TopologyScheduleResponse
import com.example.demo.pipeline.toDomain
import com.example.demo.pipeline.toDomainGroups
import com.example.demo.repository.TransactionManager
import com.example.demo.repository_jdbi.local
import computeSchedulesOptimized
//import computeScheduless
import org.springframework.stereotype.Service

class InvalidTopologyException : RuntimeException()
class InvalidDutyCycleException : RuntimeException()
class SchedulingFailedException : RuntimeException()

class TopologyNotFoundException(id: Int) :
    RuntimeException("Topology with id $id not found")


@Service
class TopologyService(
    private val planner: TopologyPlanner,
    private val transactionManager: TransactionManager
) {
    fun saveOnly(request: TopologySaveRequest): ScheduledTopologyOutput {


        val topology = request.toDomain()



        val id = transactionManager.run { tx ->
            val result = tx.topologyRepository.save(topology)
            result
        }

        val saved = transactionManager.run { tx ->
            val result = tx.topologyRepository.findById(id)
            result ?: throw TopologyNotFoundException(id)
        }


        return saved.toOutput(id, null)
    }
    fun findById(id: Int): ScheduledTopologyOutput =
        transactionManager.run { tx ->
            val topology =
                tx.topologyRepository.findById(id)
                    ?: throw TopologyNotFoundException(id)

            topology.toOutput(id, null)
        }

    fun findAll(): List<ScheduledTopologyOutput> =
        transactionManager.run { tx ->
            tx.topologyRepository
                .findAll()
                .map { (id, topology) ->
                    topology.toOutput(id, null)
                }
        }

    fun delete(id: Int) =
        transactionManager.run { tx ->
            tx.topologyRepository.delete(id)
        }





    fun updateAndReplan(
        id: Int,
        request: TopologySaveRequest
    ): ScheduledTopologyOutput {

        return transactionManager.run { tx ->

            val repo = tx.topologyRepository

            val existing = repo.findById(id)
                ?: throw TopologyNotFoundException(id)

            val topology = request.toDomain()

            val updated=repo.update(id, topology)


             updated.toOutput(id, null)
        }
    }


    fun plan(request: TopologyRequest): TopologyScheduleResponse {

        try {

            val memBefore = usedMemoryKb()
            val start = System.nanoTime()

            val topology = request.toDomain()


            val schedules = computeSchedulesOptimized(topology)
            println("sehdules -> $schedules")

            val scheduledTopology =
                planner.applySchedules(topology, schedules)

            println("scheduledTopology -> $scheduledTopology")

            //val id= repository.save(scheduledTopology)

            val end = System.nanoTime()
            val memAfter = usedMemoryKb()

            val metrics = PerformanceMetrics(
                executionTimeMs = (end - start) / 1_000_000,
                memoryUsedKb = memAfter - memBefore
            )


            return scheduledTopology.toResponse(metrics)

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }
    }

    private fun usedMemoryKb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024
    }
}

