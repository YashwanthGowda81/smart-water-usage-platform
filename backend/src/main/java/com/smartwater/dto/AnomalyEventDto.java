package com.smartwater.dto;

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
public class AnomalyEventDto {
    private Long id;
    private Long meterId;
    private String meterNumber;
    private Long householdId;
    private String flatNumber;
    private String apartmentName;
    private String eventType;
    private LocalDate readingDate;
    private BigDecimal observedValue;
    private BigDecimal baselineMean;
    private BigDecimal baselineStddev;
    private BigDecimal thresholdValue;
    private String status;
}
