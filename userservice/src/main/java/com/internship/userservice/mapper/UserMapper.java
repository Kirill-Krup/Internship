package com.internship.userservice.mapper;

import com.internship.userservice.dto.UserDTO;
import com.internship.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CardInfoMapper.class)
public interface UserMapper {

  @Mapping(target = "cards", source = "cards")
  UserDTO toDTO(User user);

  @Mapping(target = "cards", source = "cards")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toEntity(UserDTO userDTO);
}
