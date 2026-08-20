package com.internship.orderservice;

import com.internship.orderservice.dao.OrderDao;
import com.internship.orderservice.dto.OrderDTO;
import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.dto.UserInfoDTO;
import com.internship.orderservice.exception.OrderNotFoundException;
import com.internship.orderservice.exception.UserNotFoundException;
import com.internship.orderservice.fallback.UserServiceClient;
import com.internship.orderservice.mapper.OrderMapper;
import com.internship.orderservice.model.Order;
import com.internship.orderservice.model.StatusType;
import com.internship.orderservice.producer.OrderEventProducer;
import com.internship.orderservice.repository.ItemRepository;
import com.internship.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderDao orderDao;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private OrderMapper mapper;

  @Mock
  private UserServiceClient userServiceClient;

  @Mock
  private OrderEventProducer orderEventProducer;

  @InjectMocks
  private OrderServiceImpl orderService;


  @Test
  @DisplayName("Should create order with user info from email and send create-order event")
  void createOrder() {
    String email = "user@example.com";
    UserInfoDTO user = userInfo(1L, email);

    OrderDTO inputDTO = new OrderDTO(
            null,
            null,
            StatusType.PENDING,
            Collections.emptyList()
    );

    Order savedOrder = createOrderEntity(1L, StatusType.PENDING);

    OrderResponseDTO responseDTO = OrderResponseDTO.builder()
            .id(1L)
            .userId(1L)
            .status(StatusType.PENDING)
            .totalPrice(BigDecimal.ZERO)
            .build();

    when(userServiceClient.getUserByEmail(email)).thenReturn(user);
    when(mapper.toEntity(inputDTO)).thenReturn(new Order());
    when(orderDao.save(any(Order.class))).thenReturn(savedOrder);
    when(mapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);

    OrderResponseDTO result = orderService.createOrder(inputDTO, email);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getStatus()).isEqualTo(StatusType.PENDING);
    assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);

    verify(userServiceClient).getUserByEmail(email);
    verify(orderDao).save(any(Order.class));
    verify(orderEventProducer).sendCreateOrderEvent(responseDTO, BigDecimal.ZERO);
  }

  @Test
  @DisplayName("Should throw exception when user not found by email")
  void createOrder_userNotFound() {
    String email = "nonexistent@example.com";
    OrderDTO inputDTO = new OrderDTO(null, null, StatusType.PENDING, null);

    when(userServiceClient.getUserByEmail(email)).thenReturn(null);

    assertThatThrownBy(() -> orderService.createOrder(inputDTO, email))
        .isInstanceOf(UserNotFoundException.class);

    verify(orderDao, never()).save(any());
  }

  @Test
  @DisplayName("Should get order by id with user info")
  void getOrderById() {
    Long orderId = 1L;
    Order orderEntity = createOrderEntity(orderId, StatusType.CONFIRMED);
    UserInfoDTO user = userInfo(1L, "user@example.com");
    OrderResponseDTO responseDTO = OrderResponseDTO.builder()
        .id(orderId)
        .userId(1L)
        .status(StatusType.CONFIRMED)
        .build();

    when(orderDao.findById(orderId)).thenReturn(Optional.of(orderEntity));
    when(mapper.toResponseDTO(orderEntity)).thenReturn(responseDTO);
    when(userServiceClient.getUserById(1L)).thenReturn(user);

    OrderResponseDTO result = orderService.getOrderById(orderId);

    assertThat(result.getId()).isEqualTo(orderId);
    assertThat(result.getUser()).isEqualTo(user);
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException")
  void orderNotFound() {
    when(orderDao.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getOrderById(999L))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  @DisplayName("Should get orders with pagination and filters")
  void getOrders() {
    Order order = createOrderEntity(1L, StatusType.PENDING);
    UserInfoDTO user = userInfo(1L, "user@example.com");
    OrderResponseDTO responseDTO = OrderResponseDTO.builder().id(1L).userId(1L).build();
    Page<Order> page = new PageImpl<>(List.of(order));

    when(orderDao.findAll(any(Specification.class), eq(PageRequest.of(0, 10)))).thenReturn(page);
    when(mapper.toResponseDTO(order)).thenReturn(responseDTO);
    when(userServiceClient.getUserById(1L)).thenReturn(user);

    Page<OrderResponseDTO> result = orderService.getOrders(
        PageRequest.of(0, 10), LocalDateTime.now().minusDays(1), LocalDateTime.now(),
        List.of(StatusType.PENDING));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getUser()).isEqualTo(user);
  }

  @Test
  @DisplayName("Should get orders by user id")
  void getOrdersByUserId() {
    Order order = createOrderEntity(1L, StatusType.PENDING);
    UserInfoDTO user = userInfo(2L, "user@example.com");
    OrderResponseDTO responseDTO = OrderResponseDTO.builder().id(1L).userId(2L).build();

    when(orderDao.findByUserId(2L)).thenReturn(List.of(order));
    when(userServiceClient.getUserById(2L)).thenReturn(user);
    when(mapper.toResponseDTO(order)).thenReturn(responseDTO);

    List<OrderResponseDTO> result = orderService.getOrdersByUserId(2L);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUser()).isEqualTo(user);
  }

  @Test
  @DisplayName("Should update order")
  void updateOrder() {
    Long orderId = 1L;
    OrderDTO updateDTO = new OrderDTO(null, null, StatusType.SHIPPED, Collections.emptyList());
    Order existingOrder = createOrderEntity(orderId, StatusType.PENDING);
    OrderResponseDTO responseDTO = OrderResponseDTO.builder().id(orderId).status(StatusType.SHIPPED).build();
    UserInfoDTO user = userInfo(1L, "user@example.com");

    when(orderDao.findById(orderId)).thenReturn(Optional.of(existingOrder));
    when(orderDao.save(existingOrder)).thenReturn(existingOrder);
    when(mapper.toResponseDTO(existingOrder)).thenReturn(responseDTO);
    when(userServiceClient.getUserById(1L)).thenReturn(user);

    OrderResponseDTO result = orderService.updateOrder(orderId, updateDTO);

    assertThat(result.getStatus()).isEqualTo(StatusType.SHIPPED);
    assertThat(existingOrder.getStatus()).isEqualTo(StatusType.SHIPPED);
  }

  @Test
  @DisplayName("Should throw when updating non-existent order")
  void updateNotExistedOrder() {
    when(orderDao.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.updateOrder(999L, new OrderDTO(null, null, StatusType.SHIPPED, null)))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  @DisplayName("Should soft delete order")
  void deleteOrder() {
    Order order = createOrderEntity(1L, StatusType.PENDING);
    when(orderDao.findById(1L)).thenReturn(Optional.of(order));

    orderService.deleteOrder(1L);

    verify(orderDao).delete(order);
  }

  @Test
  @DisplayName("Should throw when deleting non-existent order")
  void deleteNotExistedOrder() {
    when(orderDao.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.deleteOrder(999L))
        .isInstanceOf(OrderNotFoundException.class);

    verify(orderDao, never()).delete(any());
  }

  private Order createOrderEntity(Long id, StatusType status) {
    Order order = new Order();
    order.setId(id);
    order.setUserId(1L);
    order.setStatus(status);
    order.setTotalPrice(BigDecimal.ZERO);
    order.setOrderItems(Collections.emptyList());
    order.setCreatedAt(LocalDateTime.now());
    order.setUpdatedAt(LocalDateTime.now());
    return order;
  }

  private UserInfoDTO userInfo(Long id, String email) {
    return new UserInfoDTO(id, "Name", "Surname", null, email);
  }
}
