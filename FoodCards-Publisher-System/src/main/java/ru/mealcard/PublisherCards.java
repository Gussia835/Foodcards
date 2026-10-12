package ru.mealcard;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PublisherCards {
    public static void main(String[] args) {
        SpringApplication.run(PublisherCards.class, args);
    }
}

