package com.smartwater.dto;

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
public class TariffPlanDto {
    private Long id;
    private Long apartmentId;
    private String name;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private BigDecimal baseRate;
    private Boolean active;
    private List<TariffTierDto> tiers;
}
