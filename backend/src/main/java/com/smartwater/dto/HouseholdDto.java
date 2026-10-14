package com.smartwater.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdDto {
    private Long id;
    private Long apartmentId;
    private String apartmentName;
    private String flatNumber;
    private Integer occupancy;
    private BigDecimal areaSqft;
    private Boolean metered;
    private String status;
    private OffsetDateTime createdAt;
}
