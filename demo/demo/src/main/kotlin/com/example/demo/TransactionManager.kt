package com.example.demo

import org.springframework.context.annotation.Bean

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}