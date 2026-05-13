package com.example.demo

import com.example.demo.TransactionManager
import com.example.demo.domain.NetworkTopology
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
import computeSchedulesOptimized
import jakarta.annotation.PreDestroy
//import computeScheduless
import org.springframework.stereotype.Service
import java.util.concurrent.Executors

class InvalidTopologyException : RuntimeException()
class InvalidDutyCycleException : RuntimeException()
class SchedulingFailedException : RuntimeException()

class TopologyNotFoundException(id: Int) :
    RuntimeException("Topology with id $id not found")
/*
@Service
class TopologyService2(
    private val planner: TopologyPlanner,
    private val repository: local
) {


    fun saveOnly(request: TopologySaveRequest): ScheduledTopologyOutput {
        println("=== TOPOLOGY SAVE REQUEST ===")
        println("ID: ${request.id}")

        println("SENSORS:")
        request.sensors.forEach {
            println(" - ${it.id} | (${it.x}, ${it.y}) | duty=${it.desiredDutyCycle} | tol=${it.tolerance} | group=${it.groupId}")
        }

        println("ADJACENCY:")
        request.adjacency.forEach { (k, v) ->
            println(" $k -> $v")
        }

        val topology = request.toDomain()
        println("=== TOPOLOGY DOMAIN ===")
        println("Adjacency:")
        topology.adjacency.forEach { (sensor, neighbors) ->
            println("${sensor.id} -> ${neighbors.map { it.id }}")
        }

        println("Duty cycles:")
        topology.dutyCycles.forEach { (sensor, dc) ->
            println("${sensor.id} = ${sensor.desiredDutyCycle}")
        }

        val existing = request.id?.let { repository.findById(it) }

        println("existing topology: $existing")

        val id: Int

        if (existing != null) {
            println("Updating topology with id=${request.id}")
            id = request.id!!
            repository.update(id, topology)
        } else {
            println("Creating new topology")
            id = repository.save(topology)
        }

        val saved = repository.findById(id)!!

        println("final saved topology: $saved")

        return saved.toOutput(id, null)
    }
    fun simulateServiceTopology(
        id: Int,
        slots: Int
    ): SimulationOutput {

        val topology = repository.findById(id)
            ?: throw TopologyNotFoundException(id)

        return simulateTimeSlots(
            topology = topology,
            totalSlots = slots
        )
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


       // repository.update(id, scheduledTopology)

        val metrics = PerformanceMetrics(
            executionTimeMs = (end - start) / 1_000_000,
            memoryUsedKb = memAfter - memBefore
        )

        //return scheduledTopology.toOutput(id,metrics)
        return scheduledTopology.toResponse(metrics)

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





//Thread

@Service
class TopologyService2(
    private val planner: TopologyPlanner,
    private val repository: local
) {

    // ===================== THREAD POOL =====================
    private val plannerPool =
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        )

    /* ============================================================
     * CRUD (SEM THREADS)
     * ============================================================ */

    fun saveOnly(request: TopologySaveRequest): ScheduledTopologyOutput {

        println("=== TOPOLOGY SAVE REQUEST ===")
        println("ID: ${request.id}")

        val topology = request.toDomain()

        val id: Int =
            if (request.id != null) {
                repository.update(request.id, topology)
                request.id!!
            } else {
                repository.save(topology)
            }

        val saved =
            repository.findById(id)
                ?: throw TopologyNotFoundException(id)

        return saved.toOutput(id, null)
    }

    fun simulateServiceTopology(
        id: Int,
        slots: Int
    ): SimulationOutput {

        val topology =
            repository.findById(id)
                ?: throw TopologyNotFoundException(id)

        return simulateTimeSlots(
            topology = topology,
            totalSlots = slots
        )
    }

    fun delete(id: Int) {
        repository.delete(id)
    }

    fun findById(id: Int): ScheduledTopologyOutput {
        val topology =
            repository.findById(id)
                ?: throw TopologyNotFoundException(id)

        return topology.toOutput(id, null)
    }

    fun findAll(): List<ScheduledTopologyOutput> =
        repository.findAll().map { (id, topology) ->
            topology.toOutput(id, null)
        }

    /* ============================================================
     * CORE HEAVY COMPUTATION
     * ============================================================ */

    private fun computePlanInternal(
        topology: NetworkTopology
    ): TopologyScheduleResponse {

        val memBefore = usedMemoryKb()
        val start = System.nanoTime()

        val schedules = computeSchedulesOptimized(topology)

        val scheduledTopology =
            planner.applySchedules(topology, schedules)

        val end = System.nanoTime()
        val memAfter = usedMemoryKb()

        val metrics = PerformanceMetrics(
            executionTimeMs = (end - start) / 1_000_000,
            memoryUsedKb = memAfter - memBefore
        )

        return scheduledTopology.toResponse(metrics)
    }

    /* ============================================================
     * PLAN (THREAD POOL)
     * ============================================================ */

    fun plan(request: TopologyRequest): TopologyScheduleResponse {

        try {
            val topology = request.toDomain()

            val future =
                plannerPool.submit<TopologyScheduleResponse> {
                    computePlanInternal(topology)
                }

            return future.get()

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }
    }

    /* ============================================================
     * UPDATE + REPLAN (THREAD POOL)
     * ============================================================ */

    fun updateAndReplan(
        id: Int,
        request: TopologyRequest
    ): TopologyScheduleResponse {

        // valida existência (rápido)
        repository.findById(id)
            ?: throw TopologyNotFoundException(id)

        try {
            val topology = request.toDomain()

            val future =
                plannerPool.submit<TopologyScheduleResponse> {
                    computePlanInternal(topology)
                }

            return future.get()

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }
    }

    /* ============================================================
     * UTIL
     * ============================================================ */

    private fun usedMemoryKb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024
    }

    /* ============================================================
     * SHUTDOWN
     * ============================================================ */

    @PreDestroy
    fun shutdown() {
        plannerPool.shutdown()
    }
}


*/
//hhhhhh


@Service
class TopologyService2(
    private val planner: TopologyPlanner,
    private val transactionManager: TransactionManager
) {

    fun saveOnly(request: TopologySaveRequest): ScheduledTopologyOutput {

        println("reuqest ${request.id}")

        return transactionManager.run { tx ->
            val repository = tx.topologyRepository

            val topology = request.toDomain()

            val id =
                if (request.id != null) {
                    repository.update(request.id, topology)
                    request.id
                } else {
                    repository.save(topology)
                }

            val saved =
                repository.findById(id!!)
                    ?: throw TopologyNotFoundException(id)

            saved.toOutput(id, null)
        }
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

    fun simulateServiceTopology(
        id: Int,
        slots: Int
    ): SimulationOutput =
        transactionManager.run { tx ->
            val topology =
                tx.topologyRepository.findById(id)
                    ?: throw TopologyNotFoundException(id)

            simulateTimeSlots(
                topology = topology,
                totalSlots = slots
            )
        }
    fun updateAndReplan(
        id: Int,
        request: TopologyRequest
    ): TopologyScheduleResponse =
        transactionManager.run { tx ->

            val repo = tx.topologyRepository

            repo.findById(id)
                ?: throw TopologyNotFoundException(id)

            val memBefore = usedMemoryKb()
            val start = System.nanoTime()

            val topology = request.toDomain()
            val schedules = computeSchedulesOptimized(topology)
            val scheduledTopology =
                planner.applySchedules(topology, schedules)

            //repo.update(id, scheduledTopology)

            val metrics = PerformanceMetrics(
                executionTimeMs = (System.nanoTime() - start) / 1_000_000,
                memoryUsedKb = usedMemoryKb() - memBefore
            )

            scheduledTopology.toResponse(metrics)
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


@Service
class TopologyService(
    private val planner: TopologyPlanner,
    private val transactionManager: TransactionManager
) {

    // Pool de threads para planeamento pesado
    private val plannerPool =
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        )

    /* ============================================================
     * CRUD SIMPLES (SEM THREADS – rápido)
     * ============================================================ */

    fun saveOnly(request: TopologySaveRequest): ScheduledTopologyOutput =
        transactionManager.run { tx ->

            val repository = tx.topologyRepository
            val topology = request.toDomain()

            val id =
                if (request.id != null) {
                    repository.update(request.id, topology)
                    request.id
                } else {
                    repository.save(topology)
                }

            val saved =
                repository.findById(id!!)
                    ?: throw TopologyNotFoundException(id)

            saved.toOutput(id, null)
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

    fun simulateServiceTopology(
        id: Int,
        slots: Int
    ): SimulationOutput =
        transactionManager.run { tx ->
            val topology =
                tx.topologyRepository.findById(id)
                    ?: throw TopologyNotFoundException(id)

            simulateTimeSlots(
                topology = topology,
                totalSlots = slots
            )
        }

    /* ============================================================
     * MÉTODO INTERNO PESADO (CORRE EM THREAD DO POOL)
     * ============================================================ */

    private fun computePlanInternal(
        topology: NetworkTopology
    ): TopologyScheduleResponse {

        val memBefore = usedMemoryKb()
        val start = System.nanoTime()

        val schedules =
            computeSchedulesOptimized(topology)

        val scheduledTopology =
            planner.applySchedules(topology, schedules)

        val end = System.nanoTime()
        val memAfter = usedMemoryKb()

        val metrics = PerformanceMetrics(
            executionTimeMs = (end - start) / 1_000_000,
            memoryUsedKb = memAfter - memBefore
        )

        return scheduledTopology.toResponse(metrics)
    }

    /* ============================================================
     * PLAN (HTTP → THREAD POOL)
     * ============================================================ */

    fun plan(request: TopologyRequest): TopologyScheduleResponse {
        try {
            val topology = request.toDomain()

            val future =
                plannerPool.submit<TopologyScheduleResponse> {
                    computePlanInternal(topology)
                }

            return future.get()

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }
    }

    /* ============================================================
     * UPDATE + REPLAN (HTTP → THREAD POOL)
     * ============================================================ */

    fun updateAndReplan(
        id: Int,
        request: TopologyRequest
    ): TopologyScheduleResponse {

        // valida existência (rápido)
        transactionManager.run { tx ->
            tx.topologyRepository.findById(id)
                ?: throw TopologyNotFoundException(id)
        }

        try {
            val topology = request.toDomain()

            val future =
                plannerPool.submit<TopologyScheduleResponse> {
                    computePlanInternal(topology)
                }

            return future.get()

        } catch (ex: IllegalArgumentException) {
            throw InvalidTopologyException()
        } catch (ex: Exception) {
            throw SchedulingFailedException()
        }
    }

    /* ============================================================
     * UTILITÁRIOS
     * ============================================================ */

    private fun usedMemoryKb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024
    }

    /* ============================================================
     * SHUTDOWN LIMPO
     * ============================================================ */

    @PreDestroy
    fun shutdown() {
        plannerPool.shutdown()
    }
}
