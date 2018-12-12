package ru.relex.techtalks.async;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.relex.techtalks.async.model.History;

import static ru.relex.techtalks.async.CoreConfig.CONCURRENCY;

/**
 * @author Nikita Skornyakov
 * @date 04.12.2018
 */
@Configuration
public class ReactiveMongoConfig {

  private CodecRegistry codecRegistry() {
    var classModel = ClassModel.builder(History.class).build();
    var codecProvider = PojoCodecProvider.builder().register(classModel).build();
    return CodecRegistries
      .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(codecProvider));

  }

  @Bean
  public MongoDatabase mongoReactiveDatabase(
    @Value("${spring.data.mongodb.host}") String host,
    @Value("${spring.data.mongodb.port}") String port,
    @Value("${spring.data.mongodb.database}") String database) {
//?maxPoolSize=5000
//String.format("mongodb://%s:%s", host, port)
    var client = MongoClients.create(
      MongoClientSettings.builder()
      .applyConnectionString(
        new ConnectionString(String.format("mongodb://%s:%s/?maxPoolSize="+CONCURRENCY+"&waitQueueMultiple=500&waitQueueTimeoutMS=2400000", host, port))
      ).build()
    );
    return client.getDatabase(database).withCodecRegistry(codecRegistry());

  }

}
