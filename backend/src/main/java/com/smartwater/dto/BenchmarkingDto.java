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
public class BenchmarkingDto {
    private Long householdId;
    private String flatNumber;
    private BigDecimal householdConsumptionKl;
    private BigDecimal apartmentAverageKl;
    private BigDecimal similarSizeAverageKl;
    private BigDecimal differenceKl;
    private BigDecimal percentageDifference;
    private String efficiencyRating; // "EXCELLENT", "AVERAGE", "HIGH_CONSUMPTION"
    private String recommendation;
}
