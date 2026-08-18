package com.community.trendingserviceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrendingServiceApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrendingServiceApiApplication.class, args);
    }
}
