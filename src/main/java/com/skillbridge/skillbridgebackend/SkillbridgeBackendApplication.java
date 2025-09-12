package com.skillbridge.skillbridgebackend;

import com.skillbridge.skillbridgebackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SkillbridgeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillbridgeBackendApplication.class, args);
	}



	@Bean
	CommandLineRunner testRepository(UserRepository userRepository) {
		return args -> {
			System.out.println("Testing UserRepository...");
			System.out.println("Total users: " + userRepository.count());
		};
	}
}
