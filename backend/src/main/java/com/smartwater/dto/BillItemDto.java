package com.smartwater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemDto {
    private Long id;
    private String itemType;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
