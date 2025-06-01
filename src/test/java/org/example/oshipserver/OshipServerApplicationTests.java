package org.example.oshipserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OshipServerApplicationTests {

	@Container
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@DynamicPropertySource
	static void overrideProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
	}


	@Test
	void contextLoads() throws InterruptedException {
		int i = 0;
		while (i < 3){

			Thread.sleep(1000);
			System.out.println("test : " + i++);
		}
	}

}
