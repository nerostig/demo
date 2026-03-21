package com.example.demo

import com.example.demo.pipeline.Problem
import com.example.demo.pipeline.TopologyGroupRequest
import com.example.demo.pipeline.TopologyRequest
import com.example.demo.pipeline.TopologyScheduleResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
///ERROS _4xx 0u 5xx

/// não envia topologia ->4xx
///5xx

@RestController
@RequestMapping("/api/topolgyRequest")
class TopologyController (
     private val service:TopologyService
){
//    @PostMapping("/implement")
//    fun execute(@RequestBody request: TopologyRequest): TopologyScheduleResponse {
//        return service.plan(request)
//
//
//    }
//
//    @PostMapping("/implement/group")
//    fun execute(@RequestBody request: TopologyGroupRequest): TopologyScheduleResponse {
//        return service.planGroups(request)
//    }

    @PostMapping("/implement")
    fun execute(
        @RequestBody request: TopologyRequest
    ): ResponseEntity<*> =
        try {
            ResponseEntity.ok(service.plan(request))
        } catch (ex: InvalidTopologyException) {
            Problem.response(400, Problem.invalidTopology)
        } catch (ex: InvalidDutyCycleException) {
            Problem.response(400, Problem.invalidDutyCycle)
        } catch (ex: SchedulingFailedException) {
            Problem.response(500, Problem.schedulingFailed)
        } catch (ex: Exception) {
            Problem.response(500, Problem.internalServerError)
        }

    @PostMapping("/implement/group")
    fun executeGroups(
        @RequestBody request: TopologyGroupRequest
    ): ResponseEntity<*> =
        try {
            ResponseEntity.ok(service.planGroups(request))
        } catch (ex: InvalidTopologyException) {
            Problem.response(400, Problem.invalidTopology)
        } catch (ex: InvalidDutyCycleException) {
            Problem.response(400, Problem.invalidDutyCycle)
        } catch (ex: SchedulingFailedException) {
            Problem.response(500, Problem.schedulingFailed)
        } catch (ex: Exception) {
            Problem.response(500, Problem.internalServerError)
        }


}