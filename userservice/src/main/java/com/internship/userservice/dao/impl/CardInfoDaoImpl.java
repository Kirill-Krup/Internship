package com.internship.userservice.dao.impl;

import com.internship.userservice.dao.CardInfoDao;
import com.internship.userservice.model.CardInfo;
import com.internship.userservice.repository.CardInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CardInfoDaoImpl implements CardInfoDao {

  private final CardInfoRepository cardInfoRepository;

  public CardInfoDaoImpl(CardInfoRepository cardInfoRepository) {
    this.cardInfoRepository = cardInfoRepository;
  }

  @Override
  public CardInfo save(CardInfo cardInfo) {
    return cardInfoRepository.save(cardInfo);
  }

  @Override
  public Optional<CardInfo> findById(Long id) {
    return cardInfoRepository.findById(id);
  }

  @Override
  public List<CardInfo> findByIds(List<Long> ids) {
    return cardInfoRepository.findByIdIn(ids);
  }

  @Override
  public List<CardInfo> findByUserId(Long userId) {
    return cardInfoRepository.findByUserId(userId);
  }

  @Override
  public List<CardInfo> findByUserIdJpql(Long userId) {
    return cardInfoRepository.findCardsByUserIdJPQL(userId);
  }

  @Override
  public List<CardInfo> findByUserIdNative(Long userId) {
    return cardInfoRepository.findCardsByUserIdNative(userId);
  }

  @Override
  public long countByUserId(Long userId) {
    return cardInfoRepository.countByUserId(userId);
  }

  @Override
  public Page<CardInfo> findAll(Specification<CardInfo> specification, Pageable pageable) {
    return cardInfoRepository.findAll(specification, pageable);
  }

  @Override
  public boolean existsById(Long id) {
    return cardInfoRepository.existsById(id);
  }

  @Override
  public void deleteById(Long id) {
    cardInfoRepository.deleteById(id);
  }
}
