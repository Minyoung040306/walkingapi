package com.example.walkingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WalkingapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalkingapiApplication.class, args);
	}

}
