package com.example.demo.pipeline


import org.springframework.http.ResponseEntity

class Problem(
    val message: String
) {
    companion object {

        const val MEDIA_TYPE = "application/problem+json"

        val topologyNotFound = Problem("Topology not found"
        )

        fun response(
            status: Int,
            problem: Problem
        ): ResponseEntity<Any> =
            ResponseEntity
                .status(status)
                .header("Content-Type", MEDIA_TYPE)
                .body(problem)


        val insecurePassword =
            Problem("Password does not meet security requirements")

        val userAlreadyExists =
            Problem("User already exists")

        val userOrPasswordAreInvalid =
            Problem("Username or password are invalid")

        val targetUserNotFound =
            Problem("Target user not found")

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

        val schedulingFailed =
            Problem("Failed to compute duty cycle schedule")

        val internalServerError =
            Problem("Internal server error")
    }
}