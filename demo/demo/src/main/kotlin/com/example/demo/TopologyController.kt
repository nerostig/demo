package com.example.demo

import com.example.demo.pipeline.Problem
import com.example.demo.pipeline.ScheduledTopologyOutput
import com.example.demo.pipeline.TopologyRequest
import com.example.demo.pipeline.TopologySaveRequest
import com.example.demo.services.InvalidDutyCycleException
import com.example.demo.services.InvalidTopologyException
import com.example.demo.services.SchedulingFailedException
import com.example.demo.services.TopologyNotFoundException
import com.example.demo.services.TopologyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/topology")
class TopologyController (
     private val service: TopologyService
){


    @PostMapping//("/save")
    fun save(
        @RequestBody request: TopologySaveRequest
    ): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.saveOnly(request))
        } catch (ex: InvalidTopologyException) {
            Problem.Companion.response(400, Problem.Companion.invalidTopology)
        } catch (ex: Exception) {
            Problem.Companion.response(500, Problem.Companion.internalServerError)
        }

    @PostMapping("/simulate/{id}")
    fun simulate(
        @PathVariable id: Int,
        @RequestParam slots: Int
    ): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.simulateServiceTopology(id, slots))
        } catch (ex: TopologyNotFoundException) {
            Problem.Companion.response(404, Problem.Companion.topologyNotFound)
        } catch (ex: Exception) {
            Problem.Companion.response(500, Problem.Companion.internalServerError)
        }

    @DeleteMapping("/{id}")
    fun deleteTopology(@PathVariable id: Int): ResponseEntity<Any> =
        try {
            service.delete(id)
            ResponseEntity.noContent().build()
        } catch (ex: TopologyNotFoundException) {
            Problem.Companion.response(404, Problem.Companion.topologyNotFound)
        } catch (ex: Exception) {
            Problem.Companion.response(500, Problem.Companion.internalServerError)
        }

    @PutMapping("/{id:\\d+}")
    fun updateTopology(
        @PathVariable id: Int,
        @RequestBody request: TopologySaveRequest
    ): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.updateAndReplan(id, request))
        } catch (ex: TopologyNotFoundException) {
            Problem.Companion.response(404, Problem.Companion.topologyNotFound)
        } catch (ex: InvalidTopologyException) {
            Problem.Companion.response(400, Problem.Companion.invalidTopology)
        } catch (ex: SchedulingFailedException) {
            Problem.Companion.response(500, Problem.Companion.schedulingFailed)
        }

    @PostMapping("/planning")
    fun execute(
        @RequestBody request: TopologyRequest
    ): ResponseEntity<*> =
        try {
            ResponseEntity.ok(service.plan(request))
        } catch (ex: InvalidTopologyException) {
            Problem.Companion.response(400, Problem.Companion.invalidTopology)
        } catch (ex: InvalidDutyCycleException) {
            Problem.Companion.response(400, Problem.Companion.invalidDutyCycle)
        } catch (ex: SchedulingFailedException) {
            Problem.Companion.response(500, Problem.Companion.schedulingFailed)
        } catch (ex: Exception) {
            Problem.Companion.response(500, Problem.Companion.internalServerError)
        }




    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.findById(id))
        }catch (ex: TopologyNotFoundException) {
            Problem.Companion.response(404, Problem.Companion.topologyNotFound)
        }

    @GetMapping//("/all")
    fun findAll(): ResponseEntity<List<ScheduledTopologyOutput>> =
        ResponseEntity.ok(service.findAll())


}