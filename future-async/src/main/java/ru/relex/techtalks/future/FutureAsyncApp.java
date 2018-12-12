package ru.relex.techtalks.future;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.relex.techtalks.async.AsyncCoreConfig;
import ru.relex.techtalks.async.MongoConfig;

/**
 * @author Nikita Skornyakov
 * @date 05.12.2018
 */
@SpringBootApplication
@Import({
  AsyncCoreConfig.class,
  MongoConfig.class
})
public class FutureAsyncApp {



  public static void main(String[] args) {
    SpringApplication.run(FutureAsyncApp.class);
  }
}
