package com.example.demo.pipeline


data class PasswordValidationInfo(
    val validationInfo: String?,
)


data class User(
    val id: Int,
    val username: String,
    val passwordValidation: PasswordValidationInfo,
)

class AuthenticatedUser(
    val user: User,
    val token: String,
)
