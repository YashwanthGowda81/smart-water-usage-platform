package com.smartwater;

import com.smartwater.dto.ApartmentDto;
import com.smartwater.dto.CreateApartmentRequest;
import com.smartwater.entity.Apartment;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.ApartmentRepository;
import com.smartwater.service.ApartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;

    @InjectMocks
    private ApartmentService apartmentService;

    private Apartment apartment;
    private CreateApartmentRequest createRequest;

    @BeforeEach
    void setUp() {
        apartment = Apartment.builder()
                .id(1L)
                .name("Skyline Heights")
                .address("100 Green Avenue")
                .city("Bengaluru")
                .state("Karnataka")
                .pincode("560001")
                .totalUnits(50)
                .createdAt(OffsetDateTime.now())
                .build();

        createRequest = CreateApartmentRequest.builder()
                .name("Skyline Heights")
                .address("100 Green Avenue")
                .city("Bengaluru")
                .state("Karnataka")
                .pincode("560001")
                .totalUnits(50)
                .build();
    }

    @Test
    void createApartment_Success() {
        when(apartmentRepository.existsByName("Skyline Heights")).thenReturn(false);
        when(apartmentRepository.save(any(Apartment.class))).thenReturn(apartment);

        ApartmentDto result = apartmentService.createApartment(createRequest);

        assertNotNull(result);
        assertEquals("Skyline Heights", result.getName());
        assertEquals(50, result.getTotalUnits());
        verify(apartmentRepository, times(1)).save(any(Apartment.class));
    }

    @Test
    void createApartment_DuplicateName_ThrowsDuplicateResourceException() {
        when(apartmentRepository.existsByName("Skyline Heights")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> apartmentService.createApartment(createRequest));
        verify(apartmentRepository, never()).save(any());
    }

    @Test
    void getApartmentById_Found() {
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));

        ApartmentDto result = apartmentService.getApartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Skyline Heights", result.getName());
    }

    @Test
    void getApartmentById_NotFound_ThrowsResourceNotFoundException() {
        when(apartmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apartmentService.getApartmentById(99L));
    }

    @Test
    void getAllApartments_ReturnsList() {
        when(apartmentRepository.findAll()).thenReturn(List.of(apartment));

        List<ApartmentDto> result = apartmentService.getAllApartments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Skyline Heights", result.get(0).getName());
    }

    @Test
    void deleteApartment_Success() {
        when(apartmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(apartmentRepository).deleteById(1L);

        assertDoesNotThrow(() -> apartmentService.deleteApartment(1L));
        verify(apartmentRepository, times(1)).deleteById(1L);
    }
}
