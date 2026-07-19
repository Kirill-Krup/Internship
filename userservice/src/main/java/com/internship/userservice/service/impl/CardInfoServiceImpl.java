package com.internship.userservice.service.impl;

import com.internship.userservice.dao.CardInfoDao;
import com.internship.userservice.dao.UserDao;
import com.internship.userservice.dto.CardInfoDTO;
import com.internship.userservice.dto.CreateCardInfoDTO;
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
import java.util.Optional;

@Service
public class CardInfoServiceImpl implements CardInfoService {

  private static final int MAX_CARDS_PER_USER = 5;
  private final CardInfoDao cardInfoDao;
  private final CardInfoMapper cardInfoMapper;
  private final UserDao userDao;

  public CardInfoServiceImpl(CardInfoDao cardInfoDao, CardInfoMapper cardInfoMapper,
      UserDao userDao) {
    this.cardInfoDao = cardInfoDao;
    this.cardInfoMapper = cardInfoMapper;
    this.userDao = userDao;
  }

  @Override
  @Transactional
  public CardInfoDTO createCard(CreateCardInfoDTO createCardInfoDTO) {
    User user = userDao.findById(createCardInfoDTO.getUserId())
        .orElseThrow(RuntimeException::new);
    if (cardInfoDao.countByUserId(user.getId()) >= MAX_CARDS_PER_USER) {
      throw new RuntimeException();
    }
    CardInfo entity = cardInfoMapper.toEntityForCreate(createCardInfoDTO);
    entity.setUser(user);
    entity.setActive(true);
    return cardInfoMapper.toDTO(cardInfoDao.save(entity));
  }

  @Override
  public Optional<CardInfoDTO> getCardInfoById(Long id) {
    return cardInfoDao.findById(id).map(cardInfoMapper::toDTO);
  }

  @Override
  public List<CardInfoDTO> getCardsByIds(List<Long> ids) {
    return cardInfoDao.findByIds(ids).stream().map(cardInfoMapper::toDTO).toList();
  }

  @Override
  public List<CardInfoDTO> getCardsByUserId(Long userId) {
    if (!userDao.existsById(userId)) {
      throw new RuntimeException();
    }
    return cardInfoDao.findByUserIdJpql(userId).stream().map(cardInfoMapper::toDTO).toList();
  }

  @Override
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
        .orElseThrow(RuntimeException::new);
    cardInfo.setNumber(updated.getNumber());
    cardInfo.setHolder(updated.getHolder());
    cardInfo.setExpirationDate(updated.getExpirationDate());
    if (updated.getActive() != null) {
      cardInfo.setActive(updated.getActive());
    }
    return cardInfoMapper.toDTO(cardInfoDao.save(cardInfo));
  }

  @Override
  @Transactional
  public CardInfoDTO activateCard(Long id) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(RuntimeException::new);
    cardInfo.setActive(true);
    return cardInfoMapper.toDTO(cardInfoDao.save(cardInfo));
  }

  @Override
  @Transactional
  public CardInfoDTO deactivateCard(Long id) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(RuntimeException::new);
    cardInfo.setActive(false);
    return cardInfoMapper.toDTO(cardInfoDao.save(cardInfo));
  }

  @Override
  @Transactional
  public void deleteCard(Long id) {
    CardInfo cardInfo = cardInfoDao.findById(id)
        .orElseThrow(RuntimeException::new);
    cardInfoDao.deleteById(id);
  }
}
