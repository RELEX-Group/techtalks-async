package ru.relex.techtalks.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.relex.techtalks.async.AsyncCoreConfig;
import ru.relex.techtalks.async.MongoConfig;

@SpringBootApplication
@Import({
  AsyncCoreConfig.class,
  MongoConfig.class
})
public class SimpleSpringBootApplication {
  public static void main(String[] args) {
    SpringApplication.run(SimpleSpringBootApplication.class, args);
  }
}
