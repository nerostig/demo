package com.example.demo.services

import com.example.demo.domain.SimulationOutput
import com.example.demo.domain.TopologyPlanner
import com.example.demo.domain.toOutput
import com.example.demo.domain.toResponse
import com.example.demo.pipeline.ScheduledTopologyOutput
import com.example.demo.pipeline.TopologyRequest
import com.example.demo.pipeline.TopologySaveRequest
import com.example.demo.pipeline.TopologyScheduleResponse
import com.example.demo.pipeline.toDomain
import com.example.demo.repository.TransactionManager
import computeSchedulesOptimized
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
    fun saveOnly(request: TopologySaveRequest,userId: Int): ScheduledTopologyOutput {


        val topology = request.toDomain()



        val id = transactionManager.run { tx ->
            val result = tx.topologyRepository.save(topology,userId)
            result
        }

        val saved = transactionManager.run { tx ->
            val result = tx.topologyRepository.findById(id,userId)
            result ?: throw TopologyNotFoundException(id)
        }


        return saved.toOutput(id, null)
    }
    fun findById(id: Int,userId: Int): ScheduledTopologyOutput =
        transactionManager.run { tx ->
            val topology =
                tx.topologyRepository.findById(id,userId)
                    ?: throw TopologyNotFoundException(id)

            topology.toOutput(id, null)
        }

    fun findAll(userId: Int): List<ScheduledTopologyOutput> =
        transactionManager.run { tx ->
            tx.topologyRepository
                .findAll(userId)
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
        request: TopologySaveRequest,
        userId: Int
    ): ScheduledTopologyOutput {

        return transactionManager.run { tx ->

            val repo = tx.topologyRepository

            val existing = repo.findById(id,userId)
                ?: throw TopologyNotFoundException(id)

            val topology = request.toDomain()

            val updated=repo.update(id, topology,userId)


             updated.toOutput(id, null)
        }
    }


    fun plan(request: TopologyRequest): TopologyScheduleResponse {

        try {

            val topology = request.toDomain()


            val schedules = computeSchedulesOptimized(topology)

            val scheduledTopology =
                planner.applySchedules(topology, schedules.first)




            return scheduledTopology.toResponse(schedules.second)

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }
    }


}

