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
public class SharedCostAllocationDto {
    private Long id;
    private Long billingCycleId;
    private Long householdId;
    private String flatNumber;
    private String allocationBasis;
    private BigDecimal allocationPercentage;
    private BigDecimal allocatedAmount;
}
