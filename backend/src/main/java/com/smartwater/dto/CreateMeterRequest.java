package com.smartwater.dto;

import com.smartwater.entity.MeterStatus;
import com.smartwater.entity.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMeterRequest {

    @NotNull(message = "Household ID is required")
    private Long householdId;

    @NotBlank(message = "Meter number is required")
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotNull(message = "Installation date is required")
    private LocalDate installationDate;

    private MeterStatus status; // Defaults to ACTIVE if null
}
