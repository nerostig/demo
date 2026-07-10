package com.example.demo

import com.example.demo.domain.Sha256TokenEncoder
import com.example.demo.domain.UsersDomainConfig
import com.example.demo.pipeline.AuthenticatedUserArgumentResolver
import com.example.demo.pipeline.AuthenticationInterceptor
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import kotlin.time.Duration.Companion.hours

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import org.springframework.boot.runApplication
import kotlinx.datetime.Clock
import org.springframework.web.method.support.HandlerMethodArgumentResolver

@SpringBootApplication
class DemoApplication{

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock() = Clock.System


    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
        )

    @Configuration
    class PipelineConfigurer(
        val authenticationInterceptor: AuthenticationInterceptor,
        val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,

        ) : WebMvcConfigurer {
        override fun addInterceptors(registry: InterceptorRegistry) {
            registry.addInterceptor(authenticationInterceptor)

        }

        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(authenticatedUserArgumentResolver)
        }
    }


}

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
