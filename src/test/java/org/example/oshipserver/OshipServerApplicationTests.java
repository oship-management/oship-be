package org.example.oshipserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OshipServerApplicationTests {
	private static final Logger log = LoggerFactory.getLogger(OshipServerApplicationTests.class);
	@Container
	private static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
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

	@Container
	private static final GenericContainer<?> redis = new GenericContainer<>("redis:7.0.12")
			.withExposedPorts(6379); // 기본 Redis 포트

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		String host = redis.getHost();
		Integer port = redis.getMappedPort(6379);
		registry.add("spring.redis.host", () -> host);
		registry.add("spring.redis.port", () -> port);
	}



}
