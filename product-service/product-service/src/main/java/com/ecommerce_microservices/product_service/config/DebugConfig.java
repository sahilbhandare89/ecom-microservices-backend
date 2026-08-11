package com.ecommerce_microservices.product_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebugConfig {

    @Bean
    CommandLineRunner printMongoUri(
            @Value("${spring.data.mongodb.uri}") String uri) {

        return args -> System.out.println("Mongo URI = " + uri);
    }
}