package com.smartwater.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBillingCycleRequest {
    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;
}
