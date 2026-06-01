package com.example.demo.repository_jdbi


import com.example.demo.repository.Transaction
import com.example.demo.repository.TransactionManager
import jakarta.inject.Named
import org.jdbi.v3.core.Jdbi


@Named
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
