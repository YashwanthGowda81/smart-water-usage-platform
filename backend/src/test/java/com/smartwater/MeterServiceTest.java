package com.smartwater;

import com.smartwater.dto.CreateMeterRequest;
import com.smartwater.dto.MeterDto;
import com.smartwater.entity.Apartment;
import com.smartwater.entity.Household;
import com.smartwater.entity.Meter;
import com.smartwater.entity.MeterStatus;
import com.smartwater.entity.MeterType;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.MeterRepository;
import com.smartwater.service.MeterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

    @Mock
    private MeterRepository meterRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @InjectMocks
    private MeterService meterService;

    private Household household;
    private Meter meter;
    private CreateMeterRequest createRequest;

    @BeforeEach
    void setUp() {
        Apartment apartment = Apartment.builder().id(1L).name("Skyline Heights").build();
        household = Household.builder().id(10L).flatNumber("A-101").apartment(apartment).build();

        meter = Meter.builder()
                .id(100L)
                .household(household)
                .meterNumber("MTR-1001")
                .meterType(MeterType.DIGITAL)
                .installationDate(LocalDate.of(2026, 1, 15))
                .status(MeterStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();

        createRequest = CreateMeterRequest.builder()
                .householdId(10L)
                .meterNumber("MTR-1001")
                .meterType(MeterType.DIGITAL)
                .installationDate(LocalDate.of(2026, 1, 15))
                .status(MeterStatus.ACTIVE)
                .build();
    }

    @Test
    void createMeter_Success() {
        when(meterRepository.existsByMeterNumber("MTR-1001")).thenReturn(false);
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));
        when(meterRepository.save(any(Meter.class))).thenReturn(meter);

        MeterDto result = meterService.createMeter(createRequest);

        assertNotNull(result);
        assertEquals("MTR-1001", result.getMeterNumber());
        assertEquals("A-101", result.getFlatNumber());
        verify(meterRepository, times(1)).save(any(Meter.class));
    }

    @Test
    void createMeter_DuplicateMeterNumber_ThrowsDuplicateResourceException() {
        when(meterRepository.existsByMeterNumber("MTR-1001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> meterService.createMeter(createRequest));
        verify(meterRepository, never()).save(any());
    }

    @Test
    void createMeter_HouseholdNotFound_ThrowsResourceNotFoundException() {
        when(meterRepository.existsByMeterNumber("MTR-1001")).thenReturn(false);
        when(householdRepository.findById(99L)).thenReturn(Optional.empty());
        createRequest.setHouseholdId(99L);

        assertThrows(ResourceNotFoundException.class, () -> meterService.createMeter(createRequest));
        verify(meterRepository, never()).save(any());
    }

    @Test
    void getMeterByMeterNumber_Found() {
        when(meterRepository.findByMeterNumber("MTR-1001")).thenReturn(Optional.of(meter));

        MeterDto result = meterService.getMeterByMeterNumber("MTR-1001");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("MTR-1001", result.getMeterNumber());
    }

    @Test
    void getMeterByMeterNumber_NotFound_ThrowsResourceNotFoundException() {
        when(meterRepository.findByMeterNumber("UNKNOWN-MTR")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> meterService.getMeterByMeterNumber("UNKNOWN-MTR"));
    }

    @Test
    void getMetersByHousehold_ReturnsList() {
        when(meterRepository.findByHouseholdId(10L)).thenReturn(List.of(meter));

        List<MeterDto> result = meterService.getMetersByHousehold(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MTR-1001", result.get(0).getMeterNumber());
    }
}
