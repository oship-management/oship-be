package org.example.oshipserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class OshipServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OshipServerApplication.class, args);
	}

}
