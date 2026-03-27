package com.example.demo

import com.example.demo.domain.TopologyPlanner
import com.example.demo.domain.toResponse
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

@Service
class TopologyService(
    private val planner: TopologyPlanner,
    private val repository: local
) {

    fun plan(request: TopologyRequest): TopologyScheduleResponse {
        TODO()
    }
//        try {
//            val topology = request.toDomain()
//
//            val schedules = computeSchedulesOptimized(topology)
//
//            val scheduledTopology =
//                planner.applySchedules(topology, schedules)
//
//            repository.save(scheduledTopology)
//
//            return scheduledTopology.toResponse()
//
//        } catch (ex: IllegalArgumentException) {
//            throw InvalidTopologyException()
//        } catch (ex: Exception) {
//            throw SchedulingFailedException()
//        }
//    }

    fun planGroups(request: TopologyGroupRequest): TopologyScheduleResponse {
        TODO()
//        try {
//            val topology =
//                request.toDomainGroups()
//
//            val schedules =
//                computeSchedulesOptimized(topology)
//
//            val scheduledTopology =
//                planner.applySchedules(topology, schedules)
//
//            repository.save(scheduledTopology)
//
//            return scheduledTopology.toResponse()
//
//        } catch (ex: IllegalArgumentException) {
//            throw InvalidTopologyException()
//        } catch (ex: Exception) {
//            throw SchedulingFailedException()
//        }
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

