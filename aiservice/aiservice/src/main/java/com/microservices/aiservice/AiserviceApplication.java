package com.microservices.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AiserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiserviceApplication.class, args);
	}

}
