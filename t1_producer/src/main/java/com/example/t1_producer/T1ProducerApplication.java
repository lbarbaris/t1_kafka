package com.example.t1_producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class T1ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(T1ProducerApplication.class, args);
    }

}
