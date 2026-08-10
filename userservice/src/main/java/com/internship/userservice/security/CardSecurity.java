package com.internship.userservice.security;

import com.internship.userservice.repository.CardInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("cardSecurity")
@RequiredArgsConstructor
public class CardSecurity {

    private final CardInfoRepository cardInfoRepository;

    public boolean isCardOwner(Long cardId, Long userId) {
        return cardInfoRepository.existsByIdAndUserId(cardId, userId);
    }
}
