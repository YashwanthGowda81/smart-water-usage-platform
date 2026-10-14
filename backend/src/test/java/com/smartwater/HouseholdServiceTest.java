package com.smartwater;

import com.smartwater.dto.CreateHouseholdRequest;
import com.smartwater.dto.HouseholdDto;
import com.smartwater.entity.Apartment;
import com.smartwater.entity.Household;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.ApartmentRepository;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.service.HouseholdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @InjectMocks
    private HouseholdService householdService;

    private Apartment apartment;
    private Household household;
    private CreateHouseholdRequest createRequest;

    @BeforeEach
    void setUp() {
        apartment = Apartment.builder()
                .id(1L)
                .name("Skyline Heights")
                .city("Bengaluru")
                .build();

        household = Household.builder()
                .id(10L)
                .apartment(apartment)
                .flatNumber("A-101")
                .occupancy(3)
                .areaSqft(new BigDecimal("1250.00"))
                .metered(true)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .build();

        createRequest = CreateHouseholdRequest.builder()
                .apartmentId(1L)
                .flatNumber("A-101")
                .occupancy(3)
                .areaSqft(new BigDecimal("1250.00"))
                .metered(true)
                .status("ACTIVE")
                .build();
    }

    @Test
    void createHousehold_Success() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(householdRepository.existsByApartmentIdAndFlatNumber(1L, "A-101")).thenReturn(false);
        when(householdRepository.save(any(Household.class))).thenReturn(household);

        HouseholdDto result = householdService.createHousehold(createRequest);

        assertNotNull(result);
        assertEquals("A-101", result.getFlatNumber());
        assertEquals(3, result.getOccupancy());
        assertEquals("Skyline Heights", result.getApartmentName());
        verify(householdRepository, times(1)).save(any(Household.class));
    }

    @Test
    void createHousehold_DuplicateFlatInSameApartment_ThrowsDuplicateResourceException() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(householdRepository.existsByApartmentIdAndFlatNumber(1L, "A-101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> householdService.createHousehold(createRequest));
        verify(householdRepository, never()).save(any());
    }

    @Test
    void createHousehold_ApartmentNotFound_ThrowsResourceNotFoundException() {
        when(apartmentRepository.findById(99L)).thenReturn(Optional.empty());
        createRequest.setApartmentId(99L);

        assertThrows(ResourceNotFoundException.class, () -> householdService.createHousehold(createRequest));
        verify(householdRepository, never()).save(any());
    }

    @Test
    void getHouseholdById_Found() {
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));

        HouseholdDto result = householdService.getHouseholdById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("A-101", result.getFlatNumber());
    }

    @Test
    void getHouseholdsByApartment_ReturnsList() {
        when(householdRepository.findByApartmentId(1L)).thenReturn(List.of(household));

        List<HouseholdDto> result = householdService.getHouseholdsByApartment(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A-101", result.get(0).getFlatNumber());
    }

    @Test
    void deleteHousehold_Success() {
        when(householdRepository.existsById(10L)).thenReturn(true);
        doNothing().when(householdRepository).deleteById(10L);

        assertDoesNotThrow(() -> householdService.deleteHousehold(10L));
        verify(householdRepository, times(1)).deleteById(10L);
    }
}
