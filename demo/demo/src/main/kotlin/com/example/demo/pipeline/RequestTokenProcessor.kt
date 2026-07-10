package com.example.demo.pipeline


import com.example.demo.services.TokenExternalInfo
import com.example.demo.services.UsersService
import jakarta.servlet.http.Cookie
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import kotlinx.datetime.Clock



@Component
class RequestTokenProcessor(
    val usersService: UsersService
) {


    fun processCookies(cookies: Array<Cookie>?): AuthenticatedUser? {
        if (cookies == null) return null

        // Pega todos os cookies de token do tipo token_{username}
        val tokenCookie = cookies.firstOrNull { it.name.startsWith("token_") } ?: return null
        val tokenValue = tokenCookie.value
        return usersService.getUserByToken(tokenValue)?.let {
            AuthenticatedUser(it, tokenValue)
        }
    }



    fun cookieName(username: String): String = "token_$username"

    fun createCookie(username: String, tokenExternalInfo: TokenExternalInfo): ResponseCookie {
        return ResponseCookie.from(cookieName(username), tokenExternalInfo.tokenValue)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(tokenExternalInfo.tokenExpiration.minus(Clock.System.now()).inWholeSeconds)
            .build()
    }


    fun createDeletionCookie(username: String): ResponseCookie {
        return ResponseCookie.from(cookieName(username), "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .build()
    }

    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        println("=== RequestTokenProcessor: processAuthorizationHeaderValue ===")
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }


        //val tokenBytes = Base64.getUrlDecoder().decode(parts[1]).toString()
        //println("tokenBytes $tokenBytes")
        val userEntity = usersService.getUserByToken(parts[1])
        println("userEntity from service: $userEntity")
        return try {
            userEntity?.let {
                // println("Inside let: $it")
                AuthenticatedUser(it, parts[1])
            }
        } catch (e: Exception) {
            println("Exception creating AuthenticatedUser: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    companion object {
        const val SCHEME = "bearer"

    }
}
