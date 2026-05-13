package com.example.demo

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


@SpringBootApplication
class DemoApplication

@Configuration
class DatabaseConfig {

    @Bean
    fun dataSource(): DataSource =
        PGSimpleDataSource().apply {
            setURL("jdbc:postgresql://localhost:5432/topologydb")
            user = "user"
            password = "pass"
        }
}


@Configuration
class JdbiConfig {
//
//    @Bean
//    fun jdbi(
//        @Value("\${spring.datasource.url}") url: String,
//        @Value("\${spring.datasource.username}") username: String,
//        @Value("\${spring.datasource.password}") password: String
//    ): Jdbi {
//        return Jdbi.create(url, username, password)
//    }

//    @Bean
//    fun jdbi(
//        @Value("\${spring.datasource.url}") url: String,
//        @Value("\${spring.datasource.username}") username: String,
//        @Value("\${spring.datasource.password}") password: String
//    ): Jdbi {
//        val jdbi = Jdbi.create(url, username, password)
//
//        jdbi.useHandle<Exception> { handle ->
//            val sql = this::class.java.getResource("/sql/topologies.sql")!!
//                .readText()
//
//            handle.createScript(sql).execute()
//        }
//
//        return jdbi
//    }

    @Bean
    fun jdbi(dataSource: DataSource): Jdbi {
        // Injetando o DataSource, o Spring Boot gerencia o pool de conexões (HikariCP)
        // e garante que o banco já esteja inicializado antes do Jdbi ser criado.
        return Jdbi.create(dataSource)
            .installPlugins() // Importante para suporte nativo a Kotlin e Postgres
    }



}

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
