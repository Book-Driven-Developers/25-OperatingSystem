package com.example.chapter11;

import com.example.chapter11.app.SimulatorRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Chapter11Application {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx  = SpringApplication.run(Chapter11Application.class, args);

		// 시뮬레이션이 끝나면 메인 종료
		int exitCode = SpringApplication.exit(ctx, ()->0);
		System.exit(exitCode);
	}

	@Bean
	public CommandLineRunner commandLineRunner(){
		return args -> {
			new SimulatorRunner().run(args);
		};
	}
}
