package com.internship.orderservice.service.impl;

import com.internship.orderservice.dao.OrderDao;
import com.internship.orderservice.dto.OrderDTO;
import com.internship.orderservice.dto.OrderItemDto;
import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.dto.UserInfoDTO;
import com.internship.orderservice.exception.ItemNotFoundException;
import com.internship.orderservice.exception.OrderNotFoundException;
import com.internship.orderservice.exception.UserNotFoundException;
import com.internship.orderservice.fallback.UserServiceClient;
import com.internship.orderservice.mapper.OrderMapper;
import com.internship.orderservice.model.Item;
import com.internship.orderservice.model.Order;
import com.internship.orderservice.model.OrderItem;
import com.internship.orderservice.model.StatusType;
import com.internship.orderservice.repository.ItemRepository;
import com.internship.orderservice.repository.specification.OrderSpecifications;
import com.internship.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderDao orderDao;
  private final ItemRepository itemRepository;
  private final OrderMapper mapper;
  private UserServiceClient userServiceClient;

  @Override
  public OrderResponseDTO createOrder(OrderDTO orderDTO, String email) {
    UserInfoDTO user = userServiceClient.getUserByEmail(email);
    if (user == null || user.getId() == null) {
      throw new UserNotFoundException(email);
    }
    log.info("Creating order for user: {}", user.getId());

    Order order = mapper.toEntity(orderDTO);
    order.setUserId(user.getId());
    order.setOrderItems(buildOrderItems(order, orderDTO.getOrderItems()));

    BigDecimal totalPrice = calculatePrice(order.getOrderItems());
    order.setTotalPrice(totalPrice);

    Order savedEntity = orderDao.save(order);

    return enrichWithUser(mapper.toResponseDTO(savedEntity), user);
  }

  @Override
  public OrderResponseDTO getOrderById(Long orderId) {
    Order order = orderDao.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    return enrichWithUser(mapper.toResponseDTO(order), fetchUser(order.getUserId()));
  }

  @Override
  public Page<OrderResponseDTO> getOrders(
      Pageable pageable,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      List<StatusType> statuses) {
    return orderDao.findAll(
            OrderSpecifications.withFilters(createdFrom, createdTo, statuses),
            pageable)
        .map(order -> enrichWithUser(mapper.toResponseDTO(order), fetchUser(order.getUserId())));
  }

  @Override
  public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
    UserInfoDTO user = fetchUser(userId);
    return orderDao.findByUserId(userId).stream()
        .map(mapper::toResponseDTO)
        .map(dto -> enrichWithUser(dto, user))
        .toList();
  }

  @Override
  @Transactional
  public OrderResponseDTO updateOrder(Long id, OrderDTO orderDto) {
    Order existingOrder = orderDao.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));
    existingOrder.setStatus(orderDto.getStatus());
    if (orderDto.getOrderItems() != null && !orderDto.getOrderItems().isEmpty()) {
      if (existingOrder.getOrderItems() == null) {
        existingOrder.setOrderItems(new ArrayList<>());
      } else {
        existingOrder.getOrderItems().clear();
      }
      existingOrder.getOrderItems().addAll(buildOrderItems(existingOrder, orderDto.getOrderItems()));
      existingOrder.setTotalPrice(calculatePrice(existingOrder.getOrderItems()));
    }
    Order savedEntity = orderDao.save(existingOrder);
    return enrichWithUser(mapper.toResponseDTO(savedEntity), fetchUser(savedEntity.getUserId()));
  }

  @Override
  @Transactional
  public void deleteOrder(Long orderId) {
    Order order = orderDao.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    orderDao.delete(order);
  }

  private List<OrderItem> buildOrderItems(Order order, List<OrderItemDto> itemDtos) {
    List<OrderItem> orderItems = new ArrayList<>();
    if (itemDtos == null) {
      return orderItems;
    }
    for (OrderItemDto itemDto : itemDtos) {
      if (itemDto.getItem() == null || itemDto.getItem().getId() == null) {
        continue;
      }
      Item item = itemRepository.findById(itemDto.getItem().getId())
          .orElseThrow(() -> new ItemNotFoundException(itemDto.getItem().getId()));
      OrderItem orderItem = new OrderItem();
      orderItem.setQuantity(itemDto.getQuantity());
      orderItem.setItem(item);
      orderItem.setOrder(order);
      orderItems.add(orderItem);
    }
    return orderItems;
  }

  private BigDecimal calculatePrice(List<OrderItem> orderItems) {
    if (orderItems == null || orderItems.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return orderItems.stream()
        .map(item -> item.getItem().getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private UserInfoDTO fetchUser(Long userId) {
    UserInfoDTO user = userServiceClient.getUserById(userId);
    if (user == null) {
      throw new UserNotFoundException(String.valueOf(userId));
    }
    return user;
  }

  private OrderResponseDTO enrichWithUser(OrderResponseDTO dto, UserInfoDTO user) {
    dto.setUser(user);
    return dto;
  }
}
