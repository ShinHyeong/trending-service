package com.community.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrendingServiceBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrendingServiceBatchApplication.class, args);
    }
}
