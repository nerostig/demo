package com.example.demo

import com.example.demo.pipeline.AuthenticatedUser
import com.example.demo.pipeline.Problem
import com.example.demo.pipeline.RequestTokenProcessor
import com.example.demo.pipeline.UserCreateInputModel
import com.example.demo.pipeline.UserCreateTokenInputModel
import com.example.demo.pipeline.UserGetOutputModel
import com.example.demo.pipeline.UserHomeOutputModel
import com.example.demo.pipeline.UserTokenCreateOutputModel
import com.example.demo.services.Failure
import com.example.demo.services.Success
import com.example.demo.services.TokenCreationError
import com.example.demo.services.UserCreationError
import com.example.demo.services.UsersService
import com.sun.net.httpserver.Authenticator
import http.Uris
import kotlin.toString


import jakarta.servlet.http.Cookie
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class UsersController(
    private val userService: UsersService,
    private val requestTokenProcessor: RequestTokenProcessor,

    ) {

    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        println("input")

        println(input)
        val res = userService.createUser(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(201)
                    .header(
                        "Location",
                        Uris.Users.byId(res.value).toASCIIString(),
                    ).build<Unit>()

            is Failure ->
                when (res.value) {
                    UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    UserCreationError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
                }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
            {
                val responseCookie = requestTokenProcessor.createCookie(input.username, res.value)

                ResponseEntity.status(200)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))


            }

            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        Problem.response(400, Problem.userOrPasswordAreInvalid)
                }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user: AuthenticatedUser): ResponseEntity<*> {

        val responseCookie = requestTokenProcessor.createDeletionCookie(user.user.username)

        return ResponseEntity.status(204)
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .build<Unit>()
        //userService.revokeToken(user.token)
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<*> {
        val res = userService.getUserById(id)
        return when(res) {
            is Success -> ResponseEntity.ok(
                UserGetOutputModel(
                    id = res.value.id,
                    username = res.value.username,
                )
            )
            is Failure -> Problem.response(404, Problem.targetUserNotFound)
        }

    }

    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username
        )
    }
}