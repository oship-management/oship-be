package org.example.oshipserver.domain.seller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.dto.response.TokenResponse;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.domain.seller.dto.request.SellerDeleteRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
public class SellerIntergrationTest {

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
            .waitingFor(Wait.forListeningPort());

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7.0.12")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // MySQL Testcontainer 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        // Redis Testcontainer 설정
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);
        registry.add("spring.redis.host", () -> host);
        registry.add("spring.redis.port", () -> port);

    }

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // 1. 회원가입 요청
        SellerSignupRequest signupRequest = new SellerSignupRequest(
                "seller@test.com", "password123", "SELLER",
                "홍", "길동", "01011112222",
                "셀러회사", "1234567890", "02-2222-3333",
                new AuthAddressRequest("KR", "서울", "강남", "테헤란로", "101호", "06222")
        );

        mockMvc.perform(post("/api/v1/auth/sellers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // 2. 로그인 후 access token 획득
        LoginRequest loginRequest = new LoginRequest("seller@test.com", "password123");

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
    @DisplayName("판매자 정보 조회 성공")
    @Order(1)
    void getSellerInfo_success() throws Exception {
        mockMvc.perform(get("/api/v1/sellers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("판매자 주소 수정 성공")
    @Order(2)
    void updateAddress_success() throws Exception {
        AuthAddressRequest request = new AuthAddressRequest(
                "KR", "서울", "양천구", "목동서로", "202", "07995"
        );

        mockMvc.perform(put("/api/v1/sellers/addresses")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("파트너 탈퇴 실패 비밀번호 불일치")
    void deletePartner_fail() throws Exception {
        PartnerDeleteRequest deleteRequest = new PartnerDeleteRequest("password123", "password12");

        mockMvc.perform(post("/api/v1/sellers/withdraw")
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

        mockMvc.perform(post("/api/v1/sellers/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("판매자 탈퇴 성공")
    @Order(5)
    void deleteSeller_success() throws Exception {
        SellerDeleteRequest request = new SellerDeleteRequest("password123", "password123");

        mockMvc.perform(post("/api/v1/sellers/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
