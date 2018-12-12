package ru.relex.techtalks.async.mapper;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nikita Skornyakov
 * @date 04.12.2018
 */
@Service
public class HospitalMapper {

  private static final String COLLECTION_NAME = "history";

  private final MongoDatabase database;
  private final com.mongodb.reactivestreams.client.MongoDatabase reactiveDatabase;

  public HospitalMapper(Optional<MongoDatabase> database,
                        Optional<com.mongodb.reactivestreams.client.MongoDatabase> reactiveDatabase) {
    this.database = database.orElse(null);
    this.reactiveDatabase = reactiveDatabase.orElse(null);
  }

  private final List<? extends Bson> getAvgPriceFilter(String cat, String hospitalName) {
    return List.of(
      Aggregates.match(Filters.and(
        eq("category", cat),
        eq("hospitalName", hospitalName)
        )
      ),
      Aggregates.group("$cat", Accumulators.avg("cost", "$cost")));
  }

  public BigDecimal getCategoryAvgPrice(String cat, String hospitalName) {
    var aggregated = database.getCollection(COLLECTION_NAME)
      .aggregate(getAvgPriceFilter(cat, hospitalName));

    return toBigDecimal(aggregated.first());
  }

  private BigDecimal toBigDecimal(Document document) {
    return Optional
      .ofNullable(document)
      .map(doc -> doc.get("cost", Decimal128.class))
      .map(Decimal128::bigDecimalValue)
      .orElse(BigDecimal.ZERO);
  }

  public void getCategoryAvgPriceAsync(String cat, String hospitalName, Subscriber<BigDecimal> consumer) {
    reactiveDatabase.getCollection(COLLECTION_NAME)
      .aggregate(getAvgPriceFilter(cat, hospitalName))
      .subscribe(new org.reactivestreams.Subscriber<>() {
        @Override
        public void onSubscribe(Subscription s) {
          consumer.onSubscribe(s);
        }

        @Override
        public void onNext(Document document) {
          consumer.onNext(toBigDecimal(document));
        }

        @Override
        public void onError(Throwable t) {
          consumer.onError(t);
        }

        @Override
        public void onComplete() {
          consumer.onComplete();
        }
      });
  }

  public Publisher<BigDecimal> getCategoryAvgPriceAsync(String cat, String hospitalName) {
    return Mono.from(
      reactiveDatabase.getCollection(COLLECTION_NAME)
        .aggregate(getAvgPriceFilter(cat, hospitalName))
    ).map(this::toBigDecimal);

  }

  public CompletableFuture<BigDecimal> getCategoryAvgPriceFuture(String cat, String hospitalName) {
    return Mono.from(
      reactiveDatabase.getCollection(COLLECTION_NAME)
        .aggregate(getAvgPriceFilter(cat, hospitalName))
    )
      .map(this::toBigDecimal)
      .toFuture();
  }

  public Mono<BigDecimal> getCategoryAvgPriceMono(String cat, String hospitalName) {
    return Mono.from(
      reactiveDatabase.getCollection(COLLECTION_NAME)
        .aggregate(getAvgPriceFilter(cat, hospitalName))
    ).map(this::toBigDecimal);
  }

  public long requestCount(long userId) {
    return database
      .getCollection(COLLECTION_NAME)
      .countDocuments(Filters.eq("patientId", userId));
  }

  public void requestCountAsync(long userId, Subscriber<Long> consumer) {
    reactiveDatabase
      .getCollection(COLLECTION_NAME)
      .countDocuments(Filters.eq("patientId", userId))
      .subscribe(consumer);
  }

  public Publisher<Long> requestCountAsync(long userId) {
    return reactiveDatabase
      .getCollection(COLLECTION_NAME)
      .countDocuments(Filters.eq("patientId", userId));
  }

  public Mono<Long> requestCountMono(long userId) {
    return Mono.from(reactiveDatabase
      .getCollection(COLLECTION_NAME)
      .countDocuments(Filters.eq("patientId", userId))
    );
  }

  public CompletableFuture<Long> requestCountFuture(long userId) {
    return Mono.from(
      reactiveDatabase
        .getCollection(COLLECTION_NAME)
        .countDocuments(Filters.eq("patientId", userId))
    ).toFuture();
  }

}
