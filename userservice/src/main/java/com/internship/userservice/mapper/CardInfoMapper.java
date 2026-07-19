package com.internship.userservice.mapper;

import com.internship.userservice.dto.CardInfoDTO;
import com.internship.userservice.dto.CreateCardInfoDTO;
import com.internship.userservice.model.CardInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardInfoMapper {

  @Mapping(target = "userId", source = "user.id")
  CardInfoDTO toDTO(CardInfo cardInfo);

  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  CardInfo toEntity(CardInfoDTO cardInfoDTO);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  CardInfo toEntityForCreate(CreateCardInfoDTO createCardInfoDTO);
}
