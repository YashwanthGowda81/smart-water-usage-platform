package com.smartwater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTierDto {
    private Long id;
    private Integer tierNumber;

    @com.fasterxml.jackson.annotation.JsonAlias({"minVolumeKl", "minKl"})
    private BigDecimal minKl;

    @com.fasterxml.jackson.annotation.JsonAlias({"maxVolumeKl", "maxKl"})
    private BigDecimal maxKl;

    private BigDecimal ratePerKl;
}
