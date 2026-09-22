package com.evolunteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EVolunteerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EVolunteerApplication.class, args);
    }
}
