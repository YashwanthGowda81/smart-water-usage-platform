package com.smartwater.dto;

import com.smartwater.entity.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdBillDto {
    private Long id;
    private Long billingCycleId;
    private Long householdId;
    private String flatNumber;
    private String apartmentName;
    private BigDecimal meteredConsumptionKl;
    private BigDecimal progressiveTariffCharge;
    private BigDecimal sharedCostAllocation;
    private BigDecimal sharedAreaAllocation;
    private BigDecimal adjustments;
    private BigDecimal totalPayable;
    private LocalDate dueDate;
    private BillStatus status;
    private List<BillItemDto> items;
}
