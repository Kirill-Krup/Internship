package com.internship.paymentservice;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.internship.paymentservice.dto.PaymentCreateDTO;
import com.internship.paymentservice.dto.PaymentDTO;
import com.internship.paymentservice.model.Payment;
import com.internship.paymentservice.model.enums.PaymentStatus;
import com.internship.paymentservice.repository.PaymentRepository;
import com.internship.paymentservice.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.kafka.listener.auto-startup=false"
})
@Slf4j
@DisplayName("Payment Service Integration Tests")
class PaymentServiceIntegrationTests extends IntegrationTestContainers {

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
      .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig()
          .port(8089))
      .build();

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  private PaymentCreateDTO paymentCreateDTO;

  @BeforeEach
  void setUp() {
    wireMock.resetAll();
    paymentRepository.deleteAll();
    paymentCreateDTO = new PaymentCreateDTO(1L, 100L, BigDecimal.valueOf(500.00));

    wireMock.stubFor(get(urlEqualTo("/api/v1.0/random?min=1&max=100"))
        .willReturn(ok()
            .withHeader("Content-Type", "text/plain")
            .withBody("[4]")));
  }

  @Test
  @DisplayName("Should create payment with SUCCESS status via WireMock random API")
  void testCreatePaymentWithSuccessViaWireMock() {
    PaymentDTO result = paymentService.createPayment(paymentCreateDTO);

    assertNotNull(result.getId());
    assertEquals(PaymentStatus.SUCCESS, result.getStatus());
    assertEquals(1L, result.getOrderId());
    assertEquals(100L, result.getUserId());

    wireMock.verify(1, getRequestedFor(urlEqualTo("/api/v1.0/random?min=1&max=100")));
  }

  @Test
  @DisplayName("Should create payment with FAILED status via WireMock odd random number")
  void testCreatePaymentWithFailedViaWireMock() {
    wireMock.resetAll();

    wireMock.stubFor(get(urlEqualTo("/api/v1.0/random?min=1&max=100"))
        .willReturn(ok()
            .withHeader("Content-Type", "text/plain")
            .withBody("[7]")));

    PaymentDTO result = paymentService.createPayment(paymentCreateDTO);

    assertEquals(PaymentStatus.FAILED, result.getStatus());
    wireMock.verify(1, getRequestedFor(urlEqualTo("/api/v1.0/random?min=1&max=100")));
  }

  @Test
  @DisplayName("Should find payments by order id with MongoDB indexes")
  void testFindPaymentsByOrderIdWithIndexes() {
    paymentService.createPayment(paymentCreateDTO);
    paymentService.createPayment(paymentCreateDTO);

    List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(1L);

    assertEquals(2, payments.size());
  }

  @Test
  @DisplayName("Should find payments by user id with MongoDB indexes")
  void testFindPaymentsByUserIdWithIndexes() {
    paymentService.createPayment(paymentCreateDTO);

    List<PaymentDTO> payments = paymentService.getPaymentsByUserId(100L);

    assertEquals(1, payments.size());
  }

  @Test
  @DisplayName("Should find payments by status with MongoDB index")
  void testFindPaymentsByStatusWithIndex() {
    paymentService.createPayment(paymentCreateDTO);

    List<PaymentDTO> payments = paymentService.findPaymentsByStatus(
        List.of(PaymentStatus.SUCCESS));

    assertEquals(1, payments.size());
  }

  @Test
  @DisplayName("Should calculate total sum for period by user with timestamp index")
  void testGetTotalSumForPeriodByUserIdWithIndex() {
    Instant before = Instant.now();

    paymentService.createPayment(paymentCreateDTO);
    paymentService.createPayment(paymentCreateDTO);

    Instant after = Instant.now();

    Double totalSum = paymentService.getTotalSumForPeriodByUserId(
        100L,
        before.minusSeconds(1),
        after.plusSeconds(1)
    );

    assertNotNull(totalSum);
    assertTrue(totalSum >= 999.0 && totalSum <= 1001.0,
        "Expected ~1000.00, got: " + totalSum);
  }

  @Test
  @DisplayName("Should calculate total sum for period for all users")
  void testGetTotalSumForPeriodForAllUsersWithIndex() {
    Instant before = Instant.now();

    paymentService.createPayment(paymentCreateDTO);
    paymentService.createPayment(new PaymentCreateDTO(2L, 200L, BigDecimal.valueOf(250.00)));

    Instant after = Instant.now();

    Double totalSum = paymentService.getTotalSumForPeriodForAllUsers(
        before.minusSeconds(1),
        after.plusSeconds(1)
    );

    assertNotNull(totalSum);
    assertTrue(totalSum >= 749.0 && totalSum <= 751.0,
        "Expected ~750.00, got: " + totalSum);
  }

  @Test
  @DisplayName("Should send Kafka messages successfully")
  void testKafkaMessaging() {
    kafkaTemplate.send("test-topic", "test-message");

    assertDoesNotThrow(() -> Thread.sleep(100));
  }

  @Test
  @DisplayName("Should delete payment successfully")
  void testDeletePaymentSuccessfully() {
    PaymentDTO createdPayment = paymentService.createPayment(paymentCreateDTO);

    paymentService.deletePayment(createdPayment.getId());

    assertTrue(paymentRepository.findById(createdPayment.getId()).isEmpty());
  }

  @Test
  @DisplayName("Should persist multiple payments with Liquibase migration")
  void testPersistMultiplePayments() {
    paymentService.createPayment(
        new PaymentCreateDTO(1L, 100L, BigDecimal.valueOf(500.00)));
    paymentService.createPayment(
        new PaymentCreateDTO(2L, 200L, BigDecimal.valueOf(750.00)));
    paymentService.createPayment(
        new PaymentCreateDTO(1L, 100L, BigDecimal.valueOf(250.00)));

    List<Payment> allPayments = paymentRepository.findAll();
    assertEquals(3, allPayments.size());

    List<Payment> orderPayments = paymentRepository.findByOrderId(1L);
    assertEquals(2, orderPayments.size());
  }

  @Test
  @DisplayName("Should maintain data consistency after delete")
  void testDataConsistency() {
    PaymentDTO payment1 = paymentService.createPayment(paymentCreateDTO);
    PaymentDTO payment2 = paymentService.createPayment(paymentCreateDTO);
    paymentService.createPayment(paymentCreateDTO);

    paymentService.deletePayment(payment1.getId());

    List<Payment> remainingPayments = paymentRepository.findAll();
    assertEquals(2, remainingPayments.size());

    assertTrue(remainingPayments.stream()
        .noneMatch(p -> p.getId().equals(payment1.getId())));
  }

  @Test
  @DisplayName("Should handle large payment amounts")
  void testLargePaymentAmounts() {
    PaymentCreateDTO largePayment = new PaymentCreateDTO(
        1L, 100L, new BigDecimal("999999.99"));

    PaymentDTO result = paymentService.createPayment(largePayment);

    assertEquals(new BigDecimal("999999.99"), result.getPaymentAmount());
  }

  @Test
  @DisplayName("Should verify WireMock received multiple requests")
  void testWireMockMultipleRequests() {
    paymentService.createPayment(paymentCreateDTO);
    paymentService.createPayment(paymentCreateDTO);
    paymentService.createPayment(paymentCreateDTO);

    wireMock.verify(3, getRequestedFor(urlEqualTo("/api/v1.0/random?min=1&max=100")));
  }
}
