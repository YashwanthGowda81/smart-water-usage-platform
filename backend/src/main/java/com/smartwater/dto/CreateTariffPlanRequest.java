package com.smartwater.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTariffPlanRequest {
    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotNull(message = "Effective start date is required")
    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    private BigDecimal baseRate;

    private Boolean active;

    @NotNull(message = "Tiers list is required")
    private List<TariffTierDto> tiers;
}
