package ru.relex.techtalks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.relex.techtalks.controller")
public class HospitalStubApplication {

  public static void main(String[] args) {
    SpringApplication.run(HospitalStubApplication.class);
  }
}
