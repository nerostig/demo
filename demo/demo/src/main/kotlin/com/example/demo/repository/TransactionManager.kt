package com.example.demo.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}