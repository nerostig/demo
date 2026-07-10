package com.example.demo.services


import com.example.demo.domain.Token
import com.example.demo.domain.UsersDomain
import com.example.demo.pipeline.User
import com.example.demo.repository.TransactionManager
import jakarta.inject.Named
import kotlinx.datetime.Clock

import kotlinx.datetime.Instant
import java.util.UUID

sealed class Either<out L, out R> {

    data class Left<out L>(val value: L) : Either<L, Nothing>()

    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

// Functions for when using Either to represent success or failure
fun <R> success(value: R) = Either.Right(value)

fun <L> failure(error: L) = Either.Left(error)

typealias Success<S> = Either.Right<S>
typealias Failure<F> = Either.Left<F>



data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant,
)

sealed class UserCreationError {
    data object UserAlreadyExists : UserCreationError()

    data object InsecurePassword : UserCreationError()
}
typealias UserCreationResult = Either<UserCreationError, Int>

data object UserNotFound

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}
typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>



sealed class UserBalanceError {
    data object UserNotFound : UserBalanceError()
    data object UserInMatch : UserBalanceError()
    data object InvalidAmount : UserBalanceError()
}


@Named
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) {







    fun getUserById(
        id: String
    ): Either<UserNotFound, User> {
        val userId = id.toIntOrNull() ?: return failure(UserNotFound)

        return transactionManager.run {
            val usersRepository = it.userRepository
            val user = usersRepository.getUserById(userId)
            if (user == null) {
                return@run failure(UserNotFound)
            }
            else return@run success(user)

        }
    }

    fun createUser(
        username: String,
        password: String,
    ): UserCreationResult {
        if (!usersDomain.isSafePassword(password)) {
            return failure(UserCreationError.InsecurePassword)
        }

        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val usersRepository = it.userRepository
            if (usersRepository.isUserStoredByUsername(username)) {
                failure(UserCreationError.UserAlreadyExists)
            } else {
                val id = usersRepository.storeUser(username, passwordValidationInfo)
                success(id)
            }
        }
    }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val usersRepository = it.userRepository
            val user: User =
                usersRepository.getUserByUsername(username)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                    return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
                }
            }
            val tokenValue = usersDomain.generateTokenValue()
            val now = clock.now()
            val newToken =
                Token(
                    usersDomain.createTokenValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    usersDomain.getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.userRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo.validationInfo)


            if (userAndToken != null )
            {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.userRepository.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }
}
