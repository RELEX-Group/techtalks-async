package ru.relex.techtalks.async.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.kotlin.experimental.coroutine.EnableCoroutine
import org.springframework.kotlin.experimental.coroutine.web.CoroutinesWebFluxConfigurer
import org.springframework.web.reactive.config.EnableWebFlux
import ru.relex.techtalks.async.AsyncCoreConfig
import ru.relex.techtalks.async.CoreConfig.CONCURRENCY
import ru.relex.techtalks.async.ReactiveMongoConfig
import ru.relex.techtalks.async.utils.NamedThreadFactory
import java.util.concurrent.*

@EnableWebFlux
@EnableCoroutine
@Import(
  AsyncCoreConfig::class,
  ReactiveMongoConfig::class
)
@SpringBootApplication
class KotlinSpringBootApplication {
  @Bean("JDBC_COROUTINE")
  fun jdbcCoroutine() = ThreadPoolExecutor(
    CONCURRENCY / 2,
    CONCURRENCY,
    2,
    TimeUnit.MINUTES,
    SynchronousQueue(),
    NamedThreadFactory("jdbc-dispatcher")
  ).asCoroutineDispatcher()
//  fun jdbcCoroutine() = Executors.newWorkStealingPool(CONCURRENCY).asCoroutineDispatcher()
}

@Configuration
class WebFluxConfigurer(applicationContext: ApplicationContext): CoroutinesWebFluxConfigurer(applicationContext)

fun main(args: Array<String>) {
  runApplication<KotlinSpringBootApplication>(*args)
}
