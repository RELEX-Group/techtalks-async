package ru.relex.techtalks.async.rx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.relex.techtalks.async.AsyncCoreConfig;
import ru.relex.techtalks.async.ReactiveMongoConfig;
import ru.relex.techtalks.async.utils.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ru.relex.techtalks.async.CoreConfig.CONCURRENCY;

@EnableWebFlux
@SpringBootApplication
@Import({
  AsyncCoreConfig.class,
  ReactiveMongoConfig.class
})
public class ReactorSpringBootApplication {
  @Bean("JDBC_POOL")
  public Scheduler jdbcPool() {
    return Schedulers.newParallel("jdbc", CONCURRENCY);
  }

  @Bean("ASYNC_POOL")
  public Scheduler asyncPool() {
    return Schedulers.newElastic("async");
  }

  public static void main(String[] args) {
    SpringApplication.run(ReactorSpringBootApplication.class, args);
  }
}
