package com.smartwater;

import com.smartwater.dto.CreateWaterUsageRequest;
import com.smartwater.dto.WaterUsageLogDto;
import com.smartwater.entity.Household;
import com.smartwater.entity.Meter;
import com.smartwater.entity.WaterSource;
import com.smartwater.entity.WaterUsageLog;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.InvalidReadingException;
import com.smartwater.repository.MeterRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import com.smartwater.service.WaterUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaterUsageServiceTest {

    @Mock
    private WaterUsageLogRepository waterUsageLogRepository;

    @Mock
    private MeterRepository meterRepository;

    @InjectMocks
    private WaterUsageService waterUsageService;

    private Meter meter;
    private CreateWaterUsageRequest usageRequest;

    @BeforeEach
    void setUp() {
        Household household = Household.builder()
                .id(1L)
                .flatNumber("A-101")
                .apartment(com.smartwater.entity.Apartment.builder().id(1L).name("Skyline Towers").build())
                .build();

        meter = Meter.builder()
                .id(10L)
                .meterNumber("MTR-1001")
                .household(household)
                .build();

        usageRequest = CreateWaterUsageRequest.builder()
                .meterNumber("MTR-1001")
                .readingDate(LocalDate.of(2026, 10, 1))
                .currentReading(new BigDecimal("1212.50"))
                .source(WaterSource.MANUAL)
                .build();
    }

    @Test
    void recordWaterUsage_FirstReading_CalculatesConsumptionFromZero() {
        when(meterRepository.findByMeterNumber("MTR-1001")).thenReturn(Optional.of(meter));
        when(waterUsageLogRepository.existsByMeterIdAndReadingDate(10L, LocalDate.of(2026, 10, 1))).thenReturn(false);
        when(waterUsageLogRepository.findPreviousReadings(10L, LocalDate.of(2026, 10, 1))).thenReturn(Collections.emptyList());

        WaterUsageLog savedLog = WaterUsageLog.builder()
                .id(100L)
                .meter(meter)
                .readingDate(LocalDate.of(2026, 10, 1))
                .previousReading(BigDecimal.ZERO)
                .currentReading(new BigDecimal("1212.50"))
                .consumption(new BigDecimal("1212.50"))
                .source(WaterSource.MANUAL)
                .build();

        when(waterUsageLogRepository.save(any(WaterUsageLog.class))).thenReturn(savedLog);

        WaterUsageLogDto result = waterUsageService.recordWaterUsage(usageRequest);

        assertNotNull(result);
        assertEquals(new BigDecimal("1212.50"), result.getConsumption());
        assertEquals(BigDecimal.ZERO, result.getPreviousReading());
    }

    @Test
    void recordWaterUsage_WithPreviousReading_CalculatesCorrectDifference() {
        when(meterRepository.findByMeterNumber("MTR-1001")).thenReturn(Optional.of(meter));
        when(waterUsageLogRepository.existsByMeterIdAndReadingDate(10L, LocalDate.of(2026, 10, 1))).thenReturn(false);

        WaterUsageLog previousLog = WaterUsageLog.builder()
                .currentReading(new BigDecimal("1200.00"))
                .build();
        when(waterUsageLogRepository.findPreviousReadings(10L, LocalDate.of(2026, 10, 1))).thenReturn(List.of(previousLog));

        WaterUsageLog savedLog = WaterUsageLog.builder()
                .id(101L)
                .meter(meter)
                .readingDate(LocalDate.of(2026, 10, 1))
                .previousReading(new BigDecimal("1200.00"))
                .currentReading(new BigDecimal("1212.50"))
                .consumption(new BigDecimal("12.50"))
                .source(WaterSource.MANUAL)
                .build();

        when(waterUsageLogRepository.save(any(WaterUsageLog.class))).thenReturn(savedLog);

        WaterUsageLogDto result = waterUsageService.recordWaterUsage(usageRequest);

        assertNotNull(result);
        assertEquals(new BigDecimal("12.50"), result.getConsumption());
        assertEquals(new BigDecimal("1200.00"), result.getPreviousReading());
    }

    @Test
    void recordWaterUsage_CurrentLowerThanPrevious_ThrowsInvalidReadingException() {
        when(meterRepository.findByMeterNumber("MTR-1001")).thenReturn(Optional.of(meter));
        when(waterUsageLogRepository.existsByMeterIdAndReadingDate(10L, LocalDate.of(2026, 10, 1))).thenReturn(false);

        WaterUsageLog previousLog = WaterUsageLog.builder()
                .currentReading(new BigDecimal("1500.00"))
                .build();
        when(waterUsageLogRepository.findPreviousReadings(10L, LocalDate.of(2026, 10, 1))).thenReturn(List.of(previousLog));

        assertThrows(InvalidReadingException.class, () -> waterUsageService.recordWaterUsage(usageRequest));
        verify(waterUsageLogRepository, never()).save(any());
    }

    @Test
    void recordWaterUsage_DuplicateDate_ThrowsDuplicateResourceException() {
        when(meterRepository.findByMeterNumber("MTR-1001")).thenReturn(Optional.of(meter));
        when(waterUsageLogRepository.existsByMeterIdAndReadingDate(10L, LocalDate.of(2026, 10, 1))).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> waterUsageService.recordWaterUsage(usageRequest));
        verify(waterUsageLogRepository, never()).save(any());
    }
}
