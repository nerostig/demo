package com.example.demo

import com.example.demo.domain.TopologyPlanner
import com.example.demo.domain.toOutput
import com.example.demo.domain.toResponse
import com.example.demo.pipeline.PerformanceMetrics
import com.example.demo.pipeline.ScheduledTopologyOutput
import com.example.demo.pipeline.TopologyGroupRequest
import com.example.demo.pipeline.TopologyRequest
import com.example.demo.pipeline.TopologyScheduleResponse
import com.example.demo.pipeline.toDomainGroups
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
    private val repository: local
) {

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
        request: TopologyRequest
    ):TopologyScheduleResponse  {//ScheduledTopologyOutput

        repository.findById(id)
            ?: throw TopologyNotFoundException(id)

        val memBefore = usedMemoryKb()
        val start = System.nanoTime()


        val topology = request.toDomain()


        val schedules = computeSchedulesOptimized(topology)

        val scheduledTopology =
            planner.applySchedules(topology, schedules)

        val end = System.nanoTime()
        val memAfter = usedMemoryKb()


        repository.update(id, scheduledTopology)

        val metrics = PerformanceMetrics(
            executionTimeMs = (end - start) / 1_000_000,
            memoryUsedKb = memAfter - memBefore
        )

        //return scheduledTopology.toOutput(id,metrics)
        return scheduledTopology.toResponse(id,metrics)

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

           val id= repository.save(scheduledTopology)

            val end = System.nanoTime()
            val memAfter = usedMemoryKb()

            val metrics = PerformanceMetrics(
                executionTimeMs = (end - start) / 1_000_000,
                memoryUsedKb = memAfter - memBefore
            )


            return scheduledTopology.toResponse(id,metrics)

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

            val id= repository.save(scheduledTopology)

            return scheduledTopology.toResponse(id,null)

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }


    }


//
//    fun planGroupsss(request: TopologyGroupRequest): TopologyScheduleResponse {
//
//        // 1️⃣ Converter input de grupos → domínio
//        val (topology, baseParameters) = request.toDomainGroups()
//
//        // 2️⃣ Executar algoritmo (versão para grupos)
//        val schedules = computeSchedulesForGroups(
//            topology,
//            baseParameters
//        )
//
//        // 3️⃣ Construir topologia final
//        val scheduledTopology =
//            planner.applySchedules(topology, schedules)
//
//        // 4️⃣ Persistir (em memória, por agora)
//        repository.save(scheduledTopology)
//
//        // 5️⃣ Converter para DTO de resposta
//        return scheduledTopology.toResponse()
//    }
}

