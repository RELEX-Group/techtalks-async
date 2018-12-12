package ru.relex.techtalks.async;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.relex.techtalks.async.model.History;

/**
 * @author Nikita Skornyakov
 * @date 04.12.2018
 */
@Configuration
public class MongoConfig {

  private CodecRegistry codecRegistry() {
    var classModel = ClassModel.builder(History.class).build();
    var codecProvider = PojoCodecProvider.builder().register(classModel).build();
    return CodecRegistries
      .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(codecProvider));

  }

  @Bean
  public MongoDatabase mongoDatabase(
    @Value("${spring.data.mongodb.host}") String host,
    @Value("${spring.data.mongodb.port}") String port,
    @Value("${spring.data.mongodb.database}") String database) {

    var client = MongoClients.create(String.format("mongodb://%s:%s", host, port));

    return client.getDatabase(database).withCodecRegistry(codecRegistry());
  }

}
