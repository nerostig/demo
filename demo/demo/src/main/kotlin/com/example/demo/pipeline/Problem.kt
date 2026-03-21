package com.example.demo.pipeline


import org.springframework.http.ResponseEntity

class Problem(
    val message: String
) {
    companion object {

        const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem
        ): ResponseEntity<Any> =
            ResponseEntity
                .status(status)
                .header("Content-Type", MEDIA_TYPE)
                .body(problem)

        // ===== 4xx =====
        val invalidRequestContent =
            Problem("Invalid request content")

        val invalidTopology =
            Problem("Invalid topology")

        val emptySensors =
            Problem("No sensors provided")

        val invalidLinks =
            Problem("Invalid links between sensors")

        val invalidDutyCycle =
            Problem("Invalid duty cycle parameters")

        // ===== 5xx =====
        val schedulingFailed =
            Problem("Failed to compute duty cycle schedule")

        val internalServerError =
            Problem("Internal server error")
    }
}