package com.smartwater.dto;

import com.smartwater.entity.PurchaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBulkPurchaseRequest {
    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    private Long billingCycleId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotBlank(message = "Supplier is required")
    private String supplier;

    private PurchaseType purchaseType;

    @NotNull(message = "Volume (kL) is required")
    private BigDecimal volumeKl;

    @NotNull(message = "Total cost is required")
    private BigDecimal totalCost;

    private String invoiceNumber;
    private String notes;
}
