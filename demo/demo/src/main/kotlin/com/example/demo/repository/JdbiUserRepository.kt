package com.example.demo.repository



import com.example.demo.domain.Token
import com.example.demo.domain.TokenValidationInfo
import com.example.demo.domain.UsersDomain
import com.example.demo.pipeline.PasswordValidationInfo
import com.example.demo.pipeline.User
import org.jdbi.v3.core.Handle
import org.slf4j.LoggerFactory
import kotlinx.datetime.Instant

class JdbiUserRepository(
    private val handle: Handle
) : UserRepository {

    override fun getUserById(id: Int): User? =
        handle.createQuery(
            """
            SELECT id, username, password_validation
            FROM Users
            WHERE id = :id
            """
        )
            .bind("id", id)
            .map { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    username = rs.getString("username"),
                    passwordValidation = PasswordValidationInfo(rs.getString("password_validation"))
                )
            }
            .singleOrNull()

    override fun getUserByUsername(username: String): User? =
        handle.createQuery(
            """
            SELECT id, username, password_validation
            FROM Users
            WHERE username = :username
            """
        )
            .bind("username", username)
            .map { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    username = rs.getString("username"),
                    passwordValidation = PasswordValidationInfo(rs.getString("password_validation"))
                )
            }
            .singleOrNull()

    override fun isUserStoredByUsername(username: String): Boolean =
        handle.createQuery(
            "SELECT COUNT(*) FROM Users WHERE username = :username"
        )
            .bind("username", username)
            .mapTo(Int::class.java)
            .one() > 0

    override fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo
    ): Int =
        handle.createUpdate(
            """
            INSERT INTO Users(username, password_validation)
            VALUES (:username, :password)
            """
        )
            .bind("username", username)
            .bind("password", passwordValidation.validationInfo)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .one()

    override fun createToken(token: Token, maxTokens: Int) {

        // remove tokens extra (mesma lógica do teu antigo repo)
        handle.createUpdate(
            """
            DELETE FROM Tokens
            WHERE user_id = :userId
            AND token_validation IN (
                SELECT token_validation FROM Tokens
                WHERE user_id = :userId
                ORDER BY last_used_at DESC
                OFFSET :offset
            )
            """
        )
            .bind("userId", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()

        handle.createUpdate(
            """
            INSERT INTO Tokens(token_validation, user_id, created_at, last_used_at)
            VALUES (:token, :userId, :createdAt, :lastUsedAt)
            """
        )
            .bind("token", token.tokenValidationInfo.validationInfo)
            .bind("userId", token.userId)
            .bind("createdAt", token.createdAt.epochSeconds)
            .bind("lastUsedAt", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: String): Pair<User, Token>? =
        handle.createQuery(
            """
            SELECT 
                u.id, u.username, u.password_validation,
                t.token_validation, t.created_at, t.last_used_at
            FROM Users u
            JOIN Tokens t ON u.id = t.user_id
            WHERE t.token_validation = :token
            """
        )
            .bind("token", tokenValidationInfo)
            .map { rs, _ ->
                val user = User(
                    id = rs.getInt("id"),
                    username = rs.getString("username"),
                    passwordValidation = PasswordValidationInfo(rs.getString("password_validation"))
                )

                val token = Token(
                    tokenValidationInfo = TokenValidationInfo(rs.getString("token_validation")),
                    userId = user.id,
                    createdAt = Instant.fromEpochSeconds(rs.getLong("created_at")),
                    lastUsedAt = Instant.fromEpochSeconds(rs.getLong("last_used_at"))
                )

                user to token
            }
            .singleOrNull()

    override fun updateTokenLastUsed(token: Token, now: Instant) {
        handle.createUpdate(
            """
            UPDATE Tokens
            SET last_used_at = :now
            WHERE token_validation = :token
            """
        )
            .bind("now", now.epochSeconds)
            .bind("token", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int =
        handle.createUpdate(
            """
            DELETE FROM Tokens
            WHERE token_validation = :token
            """
        )
            .bind("token", tokenValidationInfo.validationInfo)
            .execute()

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUserRepository::class.java)
    }
}