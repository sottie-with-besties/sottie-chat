package com.sottie.sottiechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class SottiechatApplication {

    public static void main(String[] args) {
        SpringApplication.run(SottiechatApplication.class, args);
    }

}
