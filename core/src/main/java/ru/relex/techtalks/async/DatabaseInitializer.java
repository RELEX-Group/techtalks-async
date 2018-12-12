package ru.relex.techtalks.async;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import ru.relex.techtalks.async.model.History;
import ru.relex.techtalks.async.model.HospitalPreset;

/**
 * @author Nikita Skornyakov
 * @date 04.12.2018
 */
public class DatabaseInitializer {

  private static final String DB_SRV = "172.20.1.6";
  private static final String PG_DB_NAME = "postgres";
  private static final String MONGO_DB_NAME = "medical";

  private static final String USER_STATEMENT =
    "INSERT INTO users (first_name, last_name, gender, dob, ext_id) VALUES "
      + "(?, ?, ?, ?, ?)\n";

  private static final String[] MALE_NAMES = {
    "James",
    "John",
    "Robert",
    "Michael",
    "William",
    "David",
    "Richard",
    "Joseph",
    "Thomas",
    "Charles",
    "Christopher",
    "Daniel",
    "Matther",
    "Anthony",
    "Donald",
    "Mark",
    "Paul",
    "Stewen",
    "Andrew",
    "Kenneth",
    "George",
    "Joshua",
    "Kevin",
    "Brian",
    "Edward",
    "Ronald",
    "Timothy",
    "Jason",
    "Jeffrey",
    "Ryan",
    "Jacob"
  };
  private static final String[] FEMALE_NAMES = {
    "Mary",
    "Patricia",
    "Jennifer",
    "Linda",
    "Elizabeth",
    "Barbara",
    "Susan",
    "Jessica",
    "Sarah",
    "Margareth",
    "Karen",
    "Nancy",
    "Lisa",
    "Betty",
    "Dorothy",
    "Sandra",
    "Ashley",
    "Kimberly",
    "Donna",
    "Emily",
    "Carol",
    "Michelle",
    "Amanda",
    "Melissa",
    "Deborah",
    "Stephanie",
    "Rebecca",
    "Laura",
    "Helen",
    "Sharon",
    "Cynthia",
    "Kathleen",
    "Amy",
    "Shirley"
  };
  private static final String[] SURNAMES = {
    "Smith",
    "Johnson",
    "Williams",
    "Brown",
    "Jones",
    "Miller",
    "Davis",
    "Garcia",
    "Rodriguez",
    "Wilson",
    "Martinez",
    "Anderson",
    "Taylor",
    "Thomas",
    "Hernandez",
    "Moore",
    "Martin",
    "Jackson",
    "Thompson",
    "White",
    "Lopez",
    "Lee",
    "Gonzalez",
    "Harris",
    "Clark",
    "Lewis",
    "Robinson",
    "Walker",
    "Perez",
    "Hall",
    "Young",
    "Allen",
    "Sanchez",
    "Wright",
    "King",
    "Scott",
    "Green",
    "Baker",
    "Adams",
    "Nelson",
    "Hill",
    "Ramirez",
    "Campbell",
    "Mitchell",
    "Roberts",
    "Carter",
    "Phillips",
    "Evans",
    "Turner",
    "Torres",
    "Parker",
    "Collins",
    "Edwards",
    "Stewart",
    "Flores"
  };
  private static final long MIN_DOB =
    LocalDateTime.of(1948, Month.JANUARY, 1, 0, 0, 0)
      .toInstant(ZoneOffset.UTC)
      .getEpochSecond();

  private static final long MAX_DOB =
    LocalDateTime.of(2018, Month.JANUARY, 1, 0, 0, 0)
      .toInstant(ZoneOffset.UTC)
      .getEpochSecond();

  private static final long MIN_INSURANCE_ISSUE_DATE = LocalDateTime.of(2012, Month.JANUARY, 1, 0, 0, 0)
    .toInstant(ZoneOffset.UTC)
    .getEpochSecond();

  private static final long MAX_INSURANCE_ISSUE_DATE = LocalDateTime.of(2016, Month.JANUARY, 1, 0, 0, 0)
    .toInstant(ZoneOffset.UTC)
    .getEpochSecond();
  private static final int TOTAL_USERS = 5_000_000;

  public static void main(String[] args) throws Exception {
    //generateDBData(args);
    generateMongoData(args);
  }

  private static void generateDBData(String[] args) throws SQLException {
    System.out.println("Generating data");

    Random rnd;
    try (Connection c = DriverManager
      .getConnection(String.format("jdbc:postgresql://%s/%s", DB_SRV, PG_DB_NAME), "root", "root")) {

      rnd = new Random();

      var userStatement = c.prepareStatement(USER_STATEMENT);

      for (int i = 0; i < TOTAL_USERS; i++) {
        if (i % (TOTAL_USERS / 10) == 0) {
          System.out.println(i / (TOTAL_USERS / 10) + "0% done");
        }

        String[] tArr;

        char gender;
        if (rnd.nextBoolean()) {
          tArr = MALE_NAMES;
          gender = 'm';
        } else {
          tArr = FEMALE_NAMES;
          gender = 'f';
        }

        var dob = LocalDate
          .ofInstant(Instant.ofEpochSecond(ThreadLocalRandom.current().nextLong(MIN_DOB, MAX_DOB)),
            ZoneOffset.UTC);

        var uuid = modifyUUID(UUID.randomUUID());

        userStatement.setString(1, tArr[rnd.nextInt(tArr.length)]);
        userStatement.setString(2, SURNAMES[rnd.nextInt(SURNAMES.length)]);
        userStatement.setString(3, String.valueOf(gender));
        userStatement.setDate(4, Date.valueOf(dob));
        userStatement.setString(5, uuid);
        userStatement.addBatch();

        if (i % 250 == 0) {
          userStatement.execute();
        }
      }
    }
  }

  private static void generateMongoData(String[] args) {
    System.out.println("Generating report data");
    Random rnd = new Random();
    var classModel = ClassModel.builder(History.class).build();
    var codecProvider = PojoCodecProvider.builder().register(classModel).build();
    var codecRegistry = CodecRegistries
      .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(codecProvider));

    var client = MongoClients.create(String.format("mongodb://%s:27017", DB_SRV));

    var database = client.getDatabase(MONGO_DB_NAME).withCodecRegistry(codecRegistry);
    var collection = database.getCollection("history", History.class);
    if (collection == null) {
      System.out.println("Creating new collection");
      database.createCollection("history");
      collection = database.getCollection("history", History.class);
    }

    List<History> hist = new ArrayList<>();
    for (long i = 1; i <= TOTAL_USERS; i++) {
      if (i % (TOTAL_USERS / 10) == 0) {
        System.out.println(i / (TOTAL_USERS / 10) + "0% done");
      }

      for (int j = 0; j < rnd.nextInt(4) + 1; j++) {
        var presets = HospitalPreset.values();
        var hospital = presets[rnd.nextInt(presets.length)];
        var cat = hospital.getCategories()[rnd.nextInt(hospital.getCategories().length)];
        hist.add(new History(UUID.randomUUID().toString(), hospital.name(), i, cat, System.currentTimeMillis(),
          hospital.nextPrice()));
      }

      if (hist.size() >= 2_500) {
        collection.insertMany(hist);
        hist.clear();
      }

    }

  }

  private static String modifyUUID(UUID uuid) {
    var uuidParts = uuid
      .toString()
      .split("-");
    return uuidParts[1] + uuidParts[4] + uuidParts[0];

  }
}
