package com.internship.paymentservice.mapper;

import com.internship.paymentservice.dto.PaymentCreateDTO;
import com.internship.paymentservice.dto.PaymentDTO;
import com.internship.paymentservice.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  Payment toEntity(PaymentDTO dto);

  PaymentDTO toDto(Payment entity);

  Payment toEntity(PaymentCreateDTO dto);
}
