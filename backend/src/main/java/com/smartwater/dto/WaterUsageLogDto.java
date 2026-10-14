package com.smartwater.dto;

import com.smartwater.entity.WaterSource;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterUsageLogDto {
    private Long id;
    private Long meterId;
    private String meterNumber;
    private String flatNumber;
    private String apartmentName;
    private LocalDate readingDate;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal consumption;
    private WaterSource source;
    private OffsetDateTime createdAt;
}
