package com.cpd.hotel_system.authentication_service;

import com.cpd.hotel_system.authentication_service.dto.request.SystemUserRequestDTO;
import com.cpd.hotel_system.authentication_service.service.SystemUserService;
import com.cpd.hotel_system.authentication_service.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
public class AuthenticationServiceApplication implements CommandLineRunner {
	private final SystemUserService service;
	private final PasswordGenerator passwordGenerator;

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		SystemUserRequestDTO user1 = new SystemUserRequestDTO(
				"ABC",
				"XYZ",
				"ABC.XYZ@gmail.com",
				passwordGenerator.generatePassword(),
				"0123456789");
		SystemUserRequestDTO user2 = new SystemUserRequestDTO(
				"DEF",
				"PQR",
				"DEF.PQR@gmail.com",
				passwordGenerator.generatePassword(),
				"9876543210");

		service.initializeHost(Arrays.asList(user1, user2));
	}
}
