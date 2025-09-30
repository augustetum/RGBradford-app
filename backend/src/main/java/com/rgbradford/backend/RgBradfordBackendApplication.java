package com.rgbradford.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.rgbradford.backend")
public class RgBradfordBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RgBradfordBackendApplication.class, args);
	}

}
