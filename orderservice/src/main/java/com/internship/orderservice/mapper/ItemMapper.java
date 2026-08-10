package com.internship.orderservice.mapper;

import com.internship.orderservice.dto.ItemDTO;
import com.internship.orderservice.model.Item;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

  ItemDTO toDTO(Item item);

  Item toEntity(ItemDTO itemDTO);

  List<ItemDTO> toDTO(List<Item> itemList);
}
