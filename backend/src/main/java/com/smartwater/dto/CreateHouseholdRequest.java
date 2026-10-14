package com.smartwater.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHouseholdRequest {

    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotNull(message = "Occupancy count is required")
    @Min(value = 1, message = "Occupancy must be at least 1")
    private Integer occupancy;

    @NotNull(message = "Area in sqft is required")
    @DecimalMin(value = "10.0", message = "Area must be at least 10 sqft")
    private BigDecimal areaSqft;

    @NotNull(message = "Metered flag is required")
    private Boolean metered;

    private String status;
}
