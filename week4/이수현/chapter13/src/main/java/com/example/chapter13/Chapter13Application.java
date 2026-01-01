package com.example.chapter13;

import com.example.chapter13.game.GameRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Chapter13Application {

	public static void main(String[] args) {
		SpringApplication.run(Chapter13Application.class, args);
	}

	@Bean
	CommandLineRunner run(GameRunner gameRunner){
		return args -> gameRunner.start();
	}
}
