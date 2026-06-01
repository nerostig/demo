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



@Service
class TopologyLocalService(
    private val planner: TopologyPlanner,
    private val repository: local
) {

    fun saveOnly(request: TopologySaveRequest): ScheduledTopologyOutput {

        val topology = request.toDomain()

        val id = repository.save(topology)

        val saved = repository.findById(id)
            ?: throw TopologyNotFoundException(id)

        return saved.toOutput(id, null)
    }


    private fun usedMemoryKb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024
    }


    fun delete(id: Int) {
        repository.delete(id)
    }

    fun findById(id: Int): ScheduledTopologyOutput {
        val topology = repository.findById(id)
            ?: throw TopologyNotFoundException(id)

        return topology.toOutput(id,null)
    }


    fun findAll(): List<ScheduledTopologyOutput> =
        repository.findAll().map { (id, topology) ->
            topology.toOutput(id,null)
        }

    fun updateAndReplan(
        id: Int,
        request: TopologySaveRequest
    ):ScheduledTopologyOutput  {//ScheduledTopologyOutput

        repository.findById(id)
            ?: throw TopologyNotFoundException(id)

        val topology = request.toDomain()


        val updated=repository.update(id, topology)



        return updated.toOutput(id, null)

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

    fun planGroups(request: TopologyGroupRequest): TopologyScheduleResponse {
        try {
            val topology =
                request.toDomainGroups()

            val schedules =
                computeSchedulesOptimized(topology)

            val scheduledTopology =
                planner.applySchedules(topology, schedules)

            // val id= repository.save(scheduledTopology)

            return scheduledTopology.toResponse(null)

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }


    }



}