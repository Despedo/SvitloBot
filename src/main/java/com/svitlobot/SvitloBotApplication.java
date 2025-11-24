package com.svitlobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SvitloBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SvitloBotApplication.class, args);
    }

}
