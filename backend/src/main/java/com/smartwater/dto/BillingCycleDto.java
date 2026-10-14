package com.smartwater.dto;

import com.smartwater.entity.BillingCycleStatus;
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
public class BillingCycleDto {
    private Long id;
    private Long apartmentId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BillingCycleStatus status;
    private BigDecimal totalWaterProcuredCost;
    private BigDecimal totalConsumptionKl;
    private String tariffSnapshotJson;
}
