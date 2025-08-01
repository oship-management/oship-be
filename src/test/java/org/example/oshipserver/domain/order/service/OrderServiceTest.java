package org.example.oshipserver.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.example.oshipserver.domain.order.dto.OrderItemDto;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks private OrderService orderService;

    private OrderCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new OrderCreateRequest(
            "SHOPIFY", "ORD123", "Test Store",
            "John", null, "john@test.com", "01012345678",
            CountryCode.KR, "Seoul", null, "Seoul", "addr1", null, "12345", null,
            "Tom", null, "tom@test.com", "01098765432",
            CountryCode.US, "New York", null, "New York", "addr1", null, "67890", null,
            "DOCUMENT", 1, "EXPRESS", 1.2, 1.2, "KG",
            10, 20, 30, "BOX", "DAP", null,
            List.of(new OrderItemDto("Item1", 1, 10.0, "USD", 0.5, "KG", "123456", "KR"))
        );
    }

    @Test
    void masterNo_정상생성_테스트() {
        Mockito.when(orderRepository.existsByOshipMasterNo(Mockito.anyString()))
            .thenReturn(false);
        String masterNo = orderService.generateUniqueMasterNo(CountryCode.US);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        assertThat(masterNo).startsWith("OSH" + today + "US");
    }

}
