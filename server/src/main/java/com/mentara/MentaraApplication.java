package com.mentara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MentaraApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentaraApplication.class, args);
    }
}