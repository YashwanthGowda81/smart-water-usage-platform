package com.smartwater.dto;

import com.smartwater.entity.PurchaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkWaterPurchaseDto {
    private Long id;
    private Long apartmentId;
    private Long billingCycleId;
    private LocalDate purchaseDate;
    private String supplier;
    private PurchaseType purchaseType;
    private BigDecimal volumeKl;
    private BigDecimal totalCost;
    private BigDecimal unitCost;
    private String invoiceNumber;
    private String notes;
    private Long createdById;
}
