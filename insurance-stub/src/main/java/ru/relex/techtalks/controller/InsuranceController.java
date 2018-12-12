package ru.relex.techtalks.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.temporal.TemporalField;
import java.util.Random;

@RestController
@RequestMapping("/")
public class InsuranceController {
    private static final Logger logger = LoggerFactory.getLogger(InsuranceController.class);

    Random r = new Random();

    @GetMapping("/")
    public Mono<Insurance> getInsurance(@RequestParam("iid") String iid) throws InterruptedException {
//        logger.info("Request for insurance {}", iid);
//        Thread.sleep(200);
//        logger.info("Insurance for {} computed", iid);
      Insurance insurance = new Insurance() {{
        this.setStatus("OK");
        this.setExpirationDate(
          LocalDateTime.of(2020, 10, 15, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
        this.setInsuranceCategory("EXTENDED");
        this.setInsuranceId(iid);
        this.setIssueDate(System.currentTimeMillis());
      }};
      return Mono.just(insurance).delayElement(Duration.ofMillis(r.nextInt(750 - 250) + 250));
    }
}
