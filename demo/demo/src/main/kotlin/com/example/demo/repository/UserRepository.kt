package com.example.demo.repository

import com.example.demo.domain.Token
import com.example.demo.domain.TokenValidationInfo
import com.example.demo.pipeline.PasswordValidationInfo
import com.example.demo.pipeline.User
import kotlinx.datetime.Instant

interface UserRepository {

    fun getUserById(id: Int): User?

    fun getUserByUsername(username: String): User?

    fun isUserStoredByUsername(username: String): Boolean

    fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo
    ): Int

    fun createToken(
        token: Token,
        maxTokens: Int
    )

    fun getTokenByTokenValidationInfo(tokenValidationInfo: String): Pair<User, Token>?

    fun updateTokenLastUsed(token: Token, now: Instant)

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}