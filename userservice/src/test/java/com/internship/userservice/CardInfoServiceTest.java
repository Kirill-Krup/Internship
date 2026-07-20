package com.internship.userservice;

import com.internship.userservice.dao.CardInfoDao;
import com.internship.userservice.dao.UserDao;
import com.internship.userservice.dto.CardInfoDTO;
import com.internship.userservice.dto.CreateCardInfoDTO;
import com.internship.userservice.exception.CardInfoNotFoundException;
import com.internship.userservice.exception.CardLimitExceededException;
import com.internship.userservice.exception.UserNotFoundException;
import com.internship.userservice.mapper.CardInfoMapper;
import com.internship.userservice.model.CardInfo;
import com.internship.userservice.model.User;
import com.internship.userservice.service.impl.CardInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardInfoServiceTest {

  @Mock
  private CardInfoDao cardInfoDao;
  @Mock
  private CardInfoMapper cardInfoMapper;
  @Mock
  private UserDao userDao;
  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache cache;

  @InjectMocks
  private CardInfoServiceImpl cardInfoService;

  private User user;
  private CardInfo card;
  private CardInfoDTO cardDTO;
  private CreateCardInfoDTO createDTO;
  private Timestamp expiration;

  @BeforeEach
  void setUp() {
    expiration = Timestamp.from(Instant.now().plus(365, ChronoUnit.DAYS));
    user = new User();
    user.setId(1L);
    user.setEmail("alice@example.com");

    card = new CardInfo();
    card.setId(10L);
    card.setNumber("4111111111111111");
    card.setHolder("Alice Smith");
    card.setExpirationDate(expiration);
    card.setActive(true);
    card.setUser(user);

    cardDTO = new CardInfoDTO(10L, "4111111111111111", "Alice Smith", expiration, true, 1L);
    createDTO = new CreateCardInfoDTO("4111111111111111", 1L, "Alice Smith", expiration);
  }

  @Test
  @DisplayName("Create card")
  void testCreateCard() {
    when(userDao.findById(1L)).thenReturn(Optional.of(user));
    when(cardInfoDao.countByUserId(1L)).thenReturn(1L);
    when(cardInfoMapper.toEntityForCreate(createDTO)).thenReturn(card);
    when(cardInfoDao.save(card)).thenReturn(card);
    when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

    CardInfoDTO result = cardInfoService.createCard(createDTO);

    assertEquals(10L, result.getId());
    verify(cardInfoDao).save(card);
  }

  @Test
  @DisplayName("Create card - user not found")
  void testCreateCard_UserNotFound() {
    when(userDao.findById(1L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> cardInfoService.createCard(createDTO));
  }

  @Test
  @DisplayName("Create card - limit exceeded")
  void testCreateCard_LimitExceeded() {
    when(userDao.findById(1L)).thenReturn(Optional.of(user));
    when(cardInfoDao.countByUserId(1L)).thenReturn(5L);
    assertThrows(CardLimitExceededException.class, () -> cardInfoService.createCard(createDTO));
    verify(cardInfoDao, never()).save(any());
  }

  @Test
  @DisplayName("Get card by id")
  void testGetCardById() {
    when(cardInfoDao.findById(10L)).thenReturn(Optional.of(card));
    when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

    Optional<CardInfoDTO> result = cardInfoService.getCardInfoById(10L);

    assertTrue(result.isPresent());
  }

  @Test
  @DisplayName("Get cards by ids")
  void testGetCardsByIds() {
    when(cardInfoDao.findByIds(List.of(10L))).thenReturn(List.of(card));
    when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

    List<CardInfoDTO> result = cardInfoService.getCardsByIds(List.of(10L));

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Get cards by user id")
  void testGetCardsByUserId() {
    when(userDao.existsById(1L)).thenReturn(true);
    when(cardInfoDao.findByUserIdJpql(1L)).thenReturn(List.of(card));
    when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

    List<CardInfoDTO> result = cardInfoService.getCardsByUserId(1L);

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Get all cards with filter")
  void testGetAllCards() {
    String name = "Alice";
    String surname = "Smith";
    Pageable pageable = PageRequest.of(0, 10);
    Page<CardInfo> cardPage = new PageImpl<>(List.of(card), pageable, 1);

   when(cardInfoDao.findAll(any(Specification.class), any(Pageable.class))).thenReturn(cardPage);
   when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

   Page<CardInfoDTO> result = cardInfoService.getAllCards(name, surname, pageable);
   assertNotNull(result);
   assertEquals(1, result.getTotalElements());
   assertEquals(1, result.getContent().size());
   assertEquals(0, result.getNumber());
   assertEquals(10, result.getSize());
   assertEquals(1, result.getTotalPages());

   assertEquals(cardDTO, result.getContent().get(0));
   verify(cardInfoDao).findAll(any(Specification.class), eq(pageable));
   verify(cardInfoMapper).toDTO(card);
  }

  @Test
  @DisplayName("Update card")
  void testUpdateCard() {
    when(cardInfoDao.findById(10L)).thenReturn(Optional.of(card));
    when(cardInfoDao.save(card)).thenReturn(card);
    when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

    CardInfoDTO result = cardInfoService.updateCard(10L, cardDTO);

    assertEquals("4111111111111111", result.getNumber());
  }

  @Test
  @DisplayName("Update card - not found")
  void testUpdateCard_NotFound() {
    when(cardInfoDao.findById(99L)).thenReturn(Optional.empty());
    assertThrows(CardInfoNotFoundException.class,
        () -> cardInfoService.updateCard(99L, cardDTO));
  }

  @Test
  @DisplayName("Activate card")
  void testActivateCard() {
    when(cardInfoDao.findById(10L)).thenReturn(Optional.of(card));
    when(cardInfoDao.save(card)).thenReturn(card);
    when(cardInfoMapper.toDTO(card)).thenReturn(cardDTO);

    CardInfoDTO result = cardInfoService.activateCard(10L);

    assertTrue(result.getActive());
    assertTrue(card.getActive());
  }

  @Test
  @DisplayName("Deactivate card")
  void testDeactivateCard() {
    CardInfoDTO inactive = new CardInfoDTO(10L, "4111111111111111", "Alice Smith", expiration,
        false, 1L);
    when(cardInfoDao.findById(10L)).thenReturn(Optional.of(card));
    when(cardInfoDao.save(card)).thenReturn(card);
    when(cardInfoMapper.toDTO(card)).thenReturn(inactive);

    CardInfoDTO result = cardInfoService.deactivateCard(10L);

    assertFalse(result.getActive());
    assertFalse(card.getActive());
  }

  @Test
  @DisplayName("Delete card")
  void testDeleteCard() {
    when(cardInfoDao.findById(10L)).thenReturn(Optional.of(card));
    when(cacheManager.getCache("users")).thenReturn(cache);

    cardInfoService.deleteCard(10L);

    verify(cardInfoDao).deleteById(10L);
    verify(cache).evict(1L);
  }

  @Test
  @DisplayName("Delete card - not found")
  void testDeleteCard_NotFound() {
    when(cardInfoDao.findById(99L)).thenReturn(Optional.empty());
    assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.deleteCard(99L));
  }
}
