package org.example.oshipserver.domain.partner;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.response.TokenResponse;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.global.common.response.BaseResponse;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
public class PartnerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accessToken;

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

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // 1. 회원가입 요청
        PartnerSignupRequest signupRequest = new PartnerSignupRequest(
                "partner@test.com", "password123", "PARTNER",
                "홍", "길동", "01011112222",
                new AuthAddressRequest("KR", "서울", "강남", "테헤란로", "101호", "06222")
        );

        mockMvc.perform(post("/api/v1/auth/partners/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // 2. 로그인 후 access token 획득
        LoginRequest loginRequest = new LoginRequest("partner@test.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JavaType responseType = objectMapper.getTypeFactory()
                .constructParametricType(BaseResponse.class, TokenResponse.class);

        BaseResponse<TokenResponse> baseResponse = objectMapper.readValue(content, responseType);
        accessToken = baseResponse.getData().accessToken();
    }

    @Test
    @Order(1)
    @DisplayName("파트너 정보 조회 성공")
    void getPartnerInfo_success() throws Exception {
        mockMvc.perform(get("/api/v1/partners")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("파트너 주소 수정 성공")
    void updateAddress_success() throws Exception {
        AuthAddressRequest updateRequest = new AuthAddressRequest(
                "KR", "서울", "서초구", "강남대로", "202호", "06611"
        );

        mockMvc.perform(put("/api/v1/partners/addresses")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("파트너 탈퇴 실패 비밀번호 불일치")
    void deletePartner_fail() throws Exception {
        PartnerDeleteRequest deleteRequest = new PartnerDeleteRequest("password123", "password12");

        mockMvc.perform(post("/api/v1/partners/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("파트너 탈퇴 실패 비밀번호 틀림")
    void deletePartner_fail2() throws Exception {
        PartnerDeleteRequest deleteRequest = new PartnerDeleteRequest("password12", "password12");

        mockMvc.perform(post("/api/v1/partners/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("파트너 탈퇴 성공")
    void deletePartner_success() throws Exception {
        PartnerDeleteRequest deleteRequest = new PartnerDeleteRequest("password123", "password123");

        mockMvc.perform(post("/api/v1/partners/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent());
    }
}
