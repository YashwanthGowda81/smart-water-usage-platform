package com.smartwater;

import com.smartwater.entity.TariffPlan;
import com.smartwater.entity.TariffTier;
import com.smartwater.repository.TariffPlanRepository;
import com.smartwater.service.TariffPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffPlanServiceTest {

    @Mock
    private TariffPlanRepository tariffPlanRepository;

    @InjectMocks
    private TariffPlanService tariffPlanService;

    private TariffPlan tariffPlan;

    @BeforeEach
    void setUp() {
        TariffTier tier1 = TariffTier.builder()
                .tierNumber(1)
                .minKl(new BigDecimal("0"))
                .maxKl(new BigDecimal("10"))
                .ratePerKl(new BigDecimal("20.00"))
                .build();

        TariffTier tier2 = TariffTier.builder()
                .tierNumber(2)
                .minKl(new BigDecimal("10"))
                .maxKl(null) // unlimited
                .ratePerKl(new BigDecimal("30.00"))
                .build();

        tariffPlan = TariffPlan.builder()
                .id(1L)
                .apartmentId(100L)
                .name("Standard Progressive Tariff")
                .effectiveStartDate(LocalDate.of(2026, 1, 1))
                .active(true)
                .tiers(List.of(tier1, tier2))
                .build();
    }

    @Test
    void calculateProgressiveTariffCharge_ExactTierExample_Returns440() {
        // Prompt Example: 18 kL total consumption -> 10 kL @ 20 + 8 kL @ 30 = 200 + 240 = 440
        when(tariffPlanRepository.findFirstByApartmentIdAndActiveTrueAndEffectiveStartDateLessThanEqualOrderByEffectiveStartDateDesc(100L, LocalDate.of(2026, 3, 31)))
                .thenReturn(Optional.of(tariffPlan));

        BigDecimal charge = tariffPlanService.calculateProgressiveTariffCharge(
                100L, LocalDate.of(2026, 3, 31), new BigDecimal("18.00")
        );

        assertEquals(new BigDecimal("440.00"), charge);
    }

    @Test
    void calculateProgressiveTariffCharge_ZeroConsumption_ReturnsZero() {
        BigDecimal charge = tariffPlanService.calculateProgressiveTariffCharge(
                100L, LocalDate.of(2026, 3, 31), BigDecimal.ZERO
        );

        assertEquals(new BigDecimal("0.00"), charge);
    }

    @Test
    void calculateProgressiveTariffCharge_WithinFirstTier_ReturnsExactRate() {
        when(tariffPlanRepository.findFirstByApartmentIdAndActiveTrueAndEffectiveStartDateLessThanEqualOrderByEffectiveStartDateDesc(100L, LocalDate.of(2026, 3, 31)))
                .thenReturn(Optional.of(tariffPlan));

        // 8 kL @ 20 = 160
        BigDecimal charge = tariffPlanService.calculateProgressiveTariffCharge(
                100L, LocalDate.of(2026, 3, 31), new BigDecimal("8.00")
        );

        assertEquals(new BigDecimal("160.00"), charge);
    }
}
