package com.sap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SapApplication.class, args);
    }
}
