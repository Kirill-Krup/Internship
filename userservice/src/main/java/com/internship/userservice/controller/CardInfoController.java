package com.internship.userservice.controller;

import com.internship.userservice.dto.CardInfoDTO;
import com.internship.userservice.dto.CreateCardInfoDTO;
import com.internship.userservice.service.CardInfoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
public class CardInfoController {

  private final CardInfoService cardInfoService;

  public CardInfoController(CardInfoService cardInfoService) {
    this.cardInfoService = cardInfoService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or #createCardInfoDTO.userId == authentication.principal.userId")
  public ResponseEntity<CardInfoDTO> createCard(
      @Valid @RequestBody CreateCardInfoDTO createCardInfoDTO) {
    CardInfoDTO createdCard = cardInfoService.createCard(createCardInfoDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<CardInfoDTO>> getAllCards(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname,
      @PageableDefault(page = 0, size = 10) Pageable pageable) {
    return ResponseEntity.ok(cardInfoService.getAllCards(name, surname, pageable));
  }

  @GetMapping("/ids")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<CardInfoDTO>> getCardsByIds(@RequestParam List<Long> ids) {
    return ResponseEntity.ok(cardInfoService.getCardsByIds(ids));
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
  public ResponseEntity<List<CardInfoDTO>> getCardsByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(cardInfoService.getCardsByUserId(userId));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isCardOwner(#id, authentication.principal.userId)")
  public ResponseEntity<CardInfoDTO> getCardById(@PathVariable Long id) {
    return ResponseEntity.ok(cardInfoService.getCardInfoById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isCardOwner(#id, authentication.principal.userId)")
  public ResponseEntity<CardInfoDTO> updateCard(@PathVariable Long id,
      @Valid @RequestBody CardInfoDTO cardInfoDTO) {
    return ResponseEntity.ok(cardInfoService.updateCard(id, cardInfoDTO));
  }

  @PutMapping("/{id}/activate")
  @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isCardOwner(#id, authentication.principal.userId)")
  public ResponseEntity<CardInfoDTO> activateCard(@PathVariable Long id) {
    return ResponseEntity.ok(cardInfoService.activateCard(id));
  }

  @PutMapping("/{id}/deactivate")
  @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isCardOwner(#id, authentication.principal.userId)")
  public ResponseEntity<CardInfoDTO> deactivateCard(@PathVariable Long id) {
    return ResponseEntity.ok(cardInfoService.deactivateCard(id));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @cardSecurity.isCardOwner(#id, authentication.principal.userId)")
  public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
    cardInfoService.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}
