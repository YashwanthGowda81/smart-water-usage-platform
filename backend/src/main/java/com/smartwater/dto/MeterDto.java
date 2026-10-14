package com.smartwater.dto;

import com.smartwater.entity.MeterStatus;
import com.smartwater.entity.MeterType;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterDto {
    private Long id;
    private Long householdId;
    private String flatNumber;
    private String apartmentName;
    private String meterNumber;
    private MeterType meterType;
    private LocalDate installationDate;
    private MeterStatus status;
    private OffsetDateTime createdAt;
}
