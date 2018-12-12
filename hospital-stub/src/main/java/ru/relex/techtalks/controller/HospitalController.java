package ru.relex.techtalks.controller;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequestMapping("/")
@RestController
public class HospitalController {

  private static final Logger logger = LoggerFactory.getLogger(HospitalController.class);

  Random r = new Random();

  @GetMapping("/")
  public Flux<Hospital> lookupHospitals(@RequestParam("loc") String loc) throws InterruptedException {
//    logger.info("Request for hospital {}", loc);
//    Thread.sleep(400);
//    logger.info("Hospital for {} computed", loc);
    Hospital hospital = new Hospital() {{
      this.setCategories(List.of("CAT1", "CAT2", "CAT3"));
      this.setLocation(loc);
      this.setTitle("HOSPITAL1");
    }};
    return Flux.fromIterable(Collections.singletonList(hospital)).delaySequence(Duration.ofMillis(r.nextInt(750 - 250) + 250));
  }
}
