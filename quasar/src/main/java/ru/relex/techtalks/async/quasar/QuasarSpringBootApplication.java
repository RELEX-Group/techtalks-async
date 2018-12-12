package ru.relex.techtalks.async.quasar;

import co.paralleluniverse.springframework.web.servlet.config.annotation.FiberWebMvcConfigurationSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.relex.techtalks.async.AsyncCoreConfig;
import ru.relex.techtalks.async.MongoConfig;
import ru.relex.techtalks.async.ReactiveMongoConfig;
import ru.relex.techtalks.async.utils.NamedThreadFactory;

import java.util.concurrent.*;

import static ru.relex.techtalks.async.CoreConfig.CONCURRENCY;

@SpringBootApplication
@Import({
  FiberWebMvcConfigurationSupport.class,
  AsyncCoreConfig.class,
  ReactiveMongoConfig.class
})
public class QuasarSpringBootApplication {

  @Bean("JDBC_POOL")
  public ExecutorService jdbcPool() {
    return new ThreadPoolExecutor(
      CONCURRENCY / 2,
      CONCURRENCY,
      2,
      TimeUnit.MINUTES,
      new ArrayBlockingQueue<>(1000),
      new NamedThreadFactory("jdbc-dispatcher")
    );
  }

  public static void main(String[] args) {
    SpringApplication.run(QuasarSpringBootApplication.class, args);
  }
}
