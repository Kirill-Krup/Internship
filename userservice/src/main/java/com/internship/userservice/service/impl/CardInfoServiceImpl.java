package com.internship.userservice.service.impl;

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
import com.internship.userservice.repository.specification.EntitySpecifications;
import com.internship.userservice.service.CardInfoService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardInfoServiceImpl implements CardInfoService {

  private static final int MAX_CARDS_PER_USER = 5;
  private static final String USERS_CACHE = "users";

  private final CardInfoDao cardInfoDao;
  private final CardInfoMapper cardInfoMapper;
  private final UserDao userDao;
  private final CacheManager cacheManager;

  public CardInfoServiceImpl(CardInfoDao cardInfoDao, CardInfoMapper cardInfoMapper,
      UserDao userDao, CacheManager cacheManager) {
    this.cardInfoDao = cardInfoDao;
    this.cardInfoMapper = cardInfoMapper;
    this.userDao = userDao;
    this.cacheManager = cacheManager;
  }

  @Override
  @Transactional
  @CacheEvict(value = USERS_CACHE, key = "#createCardInfoDTO.userId")
  public CardInfoDTO createCard(CreateCardInfoDTO createCardInfoDTO) {
    User user = userDao.findById(createCardInfoDTO.getUserId())
        .orElseThrow(() -> new UserNotFoundException(createCardInfoDTO.getUserId()));
    if (cardInfoDao.countByUserId(user.getId()) >= MAX_CARDS_PER_USER) {
      throw new CardLimitExceededException(createCardInfoDTO.getUserId());
    }
    CardInfo entity = cardInfoMapper.toEntityForCreate(createCardInfoDTO);
    entity.setUser(user);
    entity.setActive(true);
    return cardInfoMapper.toDTO(cardInfoDao.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public CardInfoDTO getCardInfoById(Long id) {
    return cardInfoDao.findById(id)
        .map(cardInfoMapper::toDTO)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CardInfoDTO> getCardsByIds(List<Long> ids) {
    return cardInfoDao.findByIds(ids).stream().map(cardInfoMapper::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<CardInfoDTO> getCardsByUserId(Long userId) {
    if (!userDao.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    return cardInfoDao.findByUserIdNative(userId).stream().map(cardInfoMapper::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CardInfoDTO> getAllCards(String name, String surname, Pageable pageable) {
    Specification<CardInfo> specification = Specification
            .allOf(
                    EntitySpecifications.cardHasUserName(name),
                    EntitySpecifications.cardHasUserSurname(surname)
            );

    return cardInfoDao.findAll(specification, pageable).map(cardInfoMapper::toDTO);
  }

  @Override
  @Transactional
  public CardInfoDTO updateCard(Long id, CardInfoDTO updated) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
    Long userId = cardInfo.getUser().getId();
    cardInfo.setNumber(updated.getNumber());
    cardInfo.setHolder(updated.getHolder());
    cardInfo.setExpirationDate(updated.getExpirationDate());
    if (updated.getActive() != null) {
      cardInfo.setActive(updated.getActive());
    }
    CardInfoDTO result = cardInfoMapper.toDTO(cardInfoDao.save(cardInfo));
    evictUserCache(userId);
    return result;
  }

  @Override
  @Transactional
  public CardInfoDTO activateCard(Long id) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
    Long userId = cardInfo.getUser().getId();
    cardInfoDao.activateById(id);
    CardInfo refreshed = cardInfoDao.findById(id)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
    CardInfoDTO result = cardInfoMapper.toDTO(refreshed);
    evictUserCache(userId);
    return result;
  }

  @Override
  @Transactional
  public CardInfoDTO deactivateCard(Long id) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
    Long userId = cardInfo.getUser().getId();
    cardInfoDao.deactivateById(id);
    CardInfo refreshed = cardInfoDao.findById(id)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
    CardInfoDTO result = cardInfoMapper.toDTO(refreshed);
    evictUserCache(userId);
    return result;
  }

  @Override
  @Transactional
  public void deleteCard(Long id) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(() -> new CardInfoNotFoundException(id));
    Long userId = cardInfo.getUser().getId();
    cardInfoDao.deleteById(id);
    evictUserCache(userId);
  }

  private void evictUserCache(Long userId) {
    Cache cache = cacheManager.getCache(USERS_CACHE);
    if (cache != null) {
      cache.evict(userId);
    }
  }
}
