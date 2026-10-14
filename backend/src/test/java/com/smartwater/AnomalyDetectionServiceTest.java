package com.smartwater;

import com.smartwater.entity.Household;
import com.smartwater.entity.Meter;
import com.smartwater.entity.WaterUsageLog;
import com.smartwater.repository.AnomalyEventRepository;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.UserRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import com.smartwater.service.AnomalyDetectionService;
import com.smartwater.service.EmailService;
import com.smartwater.service.NotificationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private AnomalyEventRepository anomalyEventRepository;

    @Mock
    private WaterUsageLogRepository waterUsageLogRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    private Meter meter;
    private Household household;

    @BeforeEach
    void setUp() {
        household = Household.builder()
                .id(1L)
                .flatNumber("A-101")
                .apartment(com.smartwater.entity.Apartment.builder().id(10L).name("Green Valley").build())
                .build();

        meter = Meter.builder()
                .id(100L)
                .meterNumber("MTR-1001")
                .household(household)
                .build();
    }

    @Test
    void evaluateHouseholdAnomalies_AbnormalSpikeExceedsThreshold_CreatesAnomalyEvent() {
        // Baseline readings with mean around 2.0 kL and small stddev
        WaterUsageLog log1 = WaterUsageLog.builder().meter(meter).readingDate(LocalDate.of(2026, 3, 1)).consumption(new BigDecimal("2.0")).build();
        WaterUsageLog log2 = WaterUsageLog.builder().meter(meter).readingDate(LocalDate.of(2026, 3, 2)).consumption(new BigDecimal("2.1")).build();
        WaterUsageLog log3 = WaterUsageLog.builder().meter(meter).readingDate(LocalDate.of(2026, 3, 3)).consumption(new BigDecimal("1.9")).build();
        WaterUsageLog log4 = WaterUsageLog.builder().meter(meter).readingDate(LocalDate.of(2026, 3, 4)).consumption(new BigDecimal("2.0")).build();
        WaterUsageLog log5 = WaterUsageLog.builder().meter(meter).readingDate(LocalDate.of(2026, 3, 5)).consumption(new BigDecimal("2.2")).build();
        // Massive spike on latest reading (15.0 kL)
        WaterUsageLog spike = WaterUsageLog.builder().meter(meter).readingDate(LocalDate.of(2026, 3, 6)).consumption(new BigDecimal("15.0")).build();

        List<WaterUsageLog> logs = List.of(spike, log5, log4, log3, log2, log1);

        when(waterUsageLogRepository.findByHouseholdIdOrderByReadingDateDesc(1L)).thenReturn(logs);
        when(userRepository.findByHouseholdId(1L)).thenReturn(Collections.emptyList());

        anomalyDetectionService.evaluateHouseholdAnomalies(1L);

        verify(anomalyEventRepository, times(1)).save(any());
    }
}
