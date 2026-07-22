package com.internship.userservice.mapper;

import com.internship.userservice.dto.UserDTO;
import com.internship.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = CardInfoMapper.class)
public interface UserMapper {

  @Mapping(target = "cards", source = "cards")
  UserDTO toDTO(User user);

  @Mapping(target = "cards", source = "cards")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toEntity(UserDTO userDTO);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "cards", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "active", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(UserDTO userDTO, @MappingTarget User user);
}
