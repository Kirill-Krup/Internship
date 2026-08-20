package com.internship.orderservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.internship.orderservice.dto.OrderDTO;
import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.exception.OrderNotFoundException;
import com.internship.orderservice.exception.UserServiceUnavailableException;
import com.internship.orderservice.model.StatusType;
import com.internship.orderservice.producer.OrderEventProducer;
import com.internship.orderservice.repository.OrderRepository;
import com.internship.orderservice.service.OrderService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@Rollback
@Testcontainers
class OrderServiceIntegrationTest {

  private static final int WIREMOCK_PORT = 9561;

  private static final String TEST_EMAIL = "kiryl.krupenin@innowise.com";

  private static final String USER_JSON = """
            {
              "id": 1,
              "name": "Kirill",
              "surname": "Krupenin",
              "birthDate": "1990-01-01T00:00:00.000+00:00",
              "email": "kiryl.krupenin@innowise.com",
              "cards": []
            }
            """;

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("order_service_db")
          .withUsername("test")
          .withPassword("test");

  private static WireMockServer wireMockServer;

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderRepository orderRepository;

  @MockitoBean
  private OrderEventProducer orderEventProducer;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

    registry.add("user.service.url", () -> "http://localhost:" + WIREMOCK_PORT);

    registry.add("jwt.secret", () -> getRequiredEnv("JWT_SECRET"));
    registry.add("jwt.expiration", () -> getRequiredEnv("JWT_EXPIRATION"));
  }

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                    .port(WIREMOCK_PORT)
                    .notifier(new ConsoleNotifier(false))
    );

    wireMockServer.start();
    configureFor("localhost", WIREMOCK_PORT);
  }

  @AfterAll
  static void stopWireMock() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @BeforeEach
  void setUp() {
    wireMockServer.resetAll();

    stubFor(get(urlMatching("/api/v1/users/email/.*"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(USER_JSON)));

    stubFor(get(urlMatching("/api/v1/users/1"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(USER_JSON)));
  }

  @Test
  @DisplayName("Should create order successfully")
  void createOrder() {
    OrderResponseDTO created = orderService.createOrder(
            orderRequest(StatusType.PENDING),
            TEST_EMAIL
    );

    verify(getRequestedFor(urlMatching("/api/v1/users/email/.*")));

    assertThat(created.getId()).isNotNull();
    assertThat(created.getStatus()).isEqualTo(StatusType.PENDING);
    assertThat(created.getUserId()).isEqualTo(1L);
    assertThat(created.getUser()).isNotNull();
    assertThat(created.getUser().getEmail()).isEqualTo(TEST_EMAIL);

    assertThat(orderRepository.findById(created.getId())).isPresent();

    verify(orderEventProducer).sendCreateOrderEvent(
            any(OrderResponseDTO.class),
            eq(BigDecimal.ZERO)
    );
  }

  @Test
  @DisplayName("Should find order by id")
  void shouldFindOrderById() {
    OrderResponseDTO created = orderService.createOrder(
            orderRequest(StatusType.CONFIRMED),
            TEST_EMAIL
    );

    OrderResponseDTO found = orderService.getOrderById(created.getId());

    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getStatus()).isEqualTo(StatusType.CONFIRMED);
    assertThat(found.getUser()).isNotNull();
    assertThat(found.getUser().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist")
  void shouldThrowOrderNotFoundException() {
    assertThatThrownBy(() -> orderService.getOrderById(999L))
            .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  @DisplayName("Should find orders with pagination and status filter")
  void shouldFindOrdersWithPaginationAndStatusFilter() {
    orderService.createOrder(orderRequest(StatusType.SHIPPED), TEST_EMAIL);
    orderService.createOrder(orderRequest(StatusType.DELIVERED), TEST_EMAIL);
    orderService.createOrder(orderRequest(StatusType.CANCELLED), TEST_EMAIL);

    Page<OrderResponseDTO> found = orderService.getOrders(
            PageRequest.of(0, 10),
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            List.of(StatusType.SHIPPED, StatusType.DELIVERED)
    );

    assertThat(found.getContent()).hasSize(2);
    assertThat(found.getContent())
            .extracting(OrderResponseDTO::getStatus)
            .containsExactlyInAnyOrder(StatusType.SHIPPED, StatusType.DELIVERED);

    assertThat(found.getContent())
            .allMatch(order -> order.getUser() != null);
  }

  @Test
  @DisplayName("Should find orders by user id")
  void shouldFindOrdersByUserId() {
    OrderResponseDTO firstOrder = orderService.createOrder(
            orderRequest(StatusType.PENDING),
            TEST_EMAIL
    );

    orderService.createOrder(orderRequest(StatusType.CONFIRMED), TEST_EMAIL);

    List<OrderResponseDTO> found = orderService.getOrdersByUserId(1L);

    assertThat(found).hasSizeGreaterThanOrEqualTo(2);
    assertThat(found)
            .allMatch(order -> order.getUserId().equals(1L));

    assertThat(firstOrder.getUser()).isNotNull();
  }

  @Test
  @DisplayName("Should update order successfully")
  void shouldUpdateOrder() {
    OrderResponseDTO created = orderService.createOrder(
            orderRequest(StatusType.PENDING),
            TEST_EMAIL
    );

    OrderResponseDTO updated = orderService.updateOrder(
            created.getId(),
            orderRequest(StatusType.SHIPPED)
    );

    assertThat(updated.getId()).isEqualTo(created.getId());
    assertThat(updated.getStatus()).isEqualTo(StatusType.SHIPPED);
    assertThat(updated.getUser()).isNotNull();
  }

  @Test
  @DisplayName("Should soft delete order successfully")
  void shouldSoftDeleteOrder() {
    OrderResponseDTO created = orderService.createOrder(
            orderRequest(StatusType.PENDING),
            TEST_EMAIL
    );

    orderService.deleteOrder(created.getId());

    assertThat(orderRepository.findById(created.getId())).isEmpty();
  }

  @Test
  @DisplayName("Should throw UserServiceUnavailableException when user service fails")
  void shouldHandleUserServiceFailure() {
    wireMockServer.resetAll();

    stubFor(get(urlMatching("/api/v1/users/email/.*"))
            .willReturn(aResponse().withStatus(503)));

    assertThatThrownBy(() -> orderService.createOrder(
            orderRequest(StatusType.PENDING),
            "nonexistent@example.com"
    )).isInstanceOf(UserServiceUnavailableException.class);
  }

  private OrderDTO orderRequest(StatusType status) {
    return new OrderDTO(
            null,
            null,
            status,
            Collections.emptyList()
    );
  }

  private static String getRequiredEnv(String key) {
    String value = System.getenv(key);

    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
              key + " environment variable is not set for tests"
      );
    }

    return value;
  }
}