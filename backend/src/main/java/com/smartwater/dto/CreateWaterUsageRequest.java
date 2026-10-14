package com.smartwater.dto;

import com.smartwater.entity.WaterSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWaterUsageRequest {

    @NotBlank(message = "Meter number is required")
    private String meterNumber;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;

    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.00", message = "Current reading cannot be negative")
    private BigDecimal currentReading;

    private WaterSource source; // Optional, defaults to MANUAL if null
}
