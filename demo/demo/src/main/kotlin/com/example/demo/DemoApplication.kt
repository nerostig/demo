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
class DatabaseConfig(
    @Value("\${spring.datasource.url}") private val url1: String,
    @Value("\${spring.datasource.username}") private val user1: String,
    @Value("\${spring.datasource.password}") private val password1: String
) {

    @Bean
    fun dataSource(): DataSource =
        PGSimpleDataSource().apply {
            setURL(url1)
            this.user = user1
            this.password = password1
//            setURL("jdbc:postgresql://localhost:5432/topologydb")
//            user = "user"
//            password = "pass"
        }
}


@Configuration
class JdbiConfig {


    @Bean
    fun jdbi(dataSource: DataSource): Jdbi {
        return Jdbi.create(dataSource)
            .installPlugins()
    }



}

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
