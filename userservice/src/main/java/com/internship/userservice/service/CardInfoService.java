package com.internship.userservice.service;

import com.internship.userservice.dto.CardInfoDTO;
import com.internship.userservice.dto.CreateCardInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CardInfoService {

  CardInfoDTO createCard(CreateCardInfoDTO createCardInfoDTO);

  Optional<CardInfoDTO> getCardInfoById(Long id);

  List<CardInfoDTO> getCardsByIds(List<Long> ids);

  List<CardInfoDTO> getCardsByUserId(Long userId);

  Page<CardInfoDTO> getAllCards(String name, String surname, Pageable pageable);

  CardInfoDTO updateCard(Long id, CardInfoDTO updated);

  CardInfoDTO activateCard(Long id);

  CardInfoDTO deactivateCard(Long id);

  void deleteCard(Long id);
}
