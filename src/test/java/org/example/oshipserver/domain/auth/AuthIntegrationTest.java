package org.example.oshipserver.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withStartupTimeout(Duration.ofSeconds(30))
            .waitingFor(Wait.forListeningPort());

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7.0.12")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofSeconds(30));

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);
        registry.add("spring.data.redis.host", () -> host);
        registry.add("spring.data.redis.port", () -> port);
    }


    @Test
    @DisplayName("파트너 회원가입 성공")
    @Order(1)
    void signupPartner() throws Exception {
        PartnerSignupRequest request = new PartnerSignupRequest(
                "partner@example.com", "password123", "PARTNER",
                "파트너회사", "02-1234-5678", "1234567890",
                new AuthAddressRequest("KR", "Seoul", "Gangnam", "도로명", "상세주소", "06112")
        );

        mockMvc.perform(post("/api/v1/auth/partners/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("셀러 회원가입 성공")
    @Order(2)
    void signupSeller() throws Exception {
        SellerSignupRequest request = new SellerSignupRequest(
                "seller@example.com", "password123", "SELLER", "성", "이름",
                "01012345678", "셀러회사", "0987654321", "02-5678-1234",
                new AuthAddressRequest("KR", "Seoul", "Jongno", "도로명", "상세주소", "03001")
        );

        mockMvc.perform(post("/api/v1/auth/sellers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("파트너 회원가입 - 중복 이메일 예외")
    void signupPartner_DuplicateEmail_ThrowsException() throws Exception {
        PartnerSignupRequest request = new PartnerSignupRequest(
                "partner@example.com", "password123", "PARTNER",
                "파트너회사", "02-1234-5678", "1234567890",
                new AuthAddressRequest("KR", "Seoul", "Gangnam", "도로명", "상세주소", "06112")
        );

        mockMvc.perform(post("/api/v1/auth/partners/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("셀러 회원가입 - 중복 이메일 예외")
    void signupSeller_DuplicateEmail_ThrowsException() throws Exception {
        SellerSignupRequest request = new SellerSignupRequest(
                "seller@example.com", "password123", "SELLER",
                "성", "이름", "01012345678",
                "셀러회사", "0987654321", "02-5678-1234",
                new AuthAddressRequest("KR", "Seoul", "Jongno", "도로명", "상세주소", "03001")
        );


        mockMvc.perform(post("/api/v1/auth/sellers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        // 먼저 회원가입 선행 필요
        //signupSeller();  // 또는 signupPartner()
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .until(redis::isRunning);


        LoginRequest sellerRequest = new LoginRequest("seller@example.com", "password123");
        LoginRequest partnerRequest = new LoginRequest("partner@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_Fail_EmailNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // or status().is4xxClientError() if NOT_FOUND mapped to 404
    }
    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_InvalidPassword() throws Exception {

        LoginRequest sellerRequest = new LoginRequest("seller@example.com", "password");
        LoginRequest partnerRequest = new LoginRequest("partner@example.com", "password");


        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellerRequest)))
                .andExpect(status().isInternalServerError());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerRequest)))
                .andExpect(status().isInternalServerError());
    }

    // refreshToken, logout은 JWT 발급 및 Redis 캐시 확인 등의 Mock 또는 시나리오 필요

}