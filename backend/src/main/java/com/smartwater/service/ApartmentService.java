package com.smartwater.service;

import com.smartwater.dto.ApartmentDto;
import com.smartwater.dto.CreateApartmentRequest;
import com.smartwater.entity.Apartment;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Transactional
    public ApartmentDto createApartment(CreateApartmentRequest request) {
        if (apartmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Apartment already exists with name: " + request.getName());
        }

        Apartment apartment = Apartment.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .totalUnits(request.getTotalUnits())
                .build();

        Apartment saved = apartmentRepository.save(apartment);
        return mapToDto(saved);
    }

    public List<ApartmentDto> getAllApartments() {
        return apartmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ApartmentDto getApartmentById(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with ID: " + id));
        return mapToDto(apartment);
    }

    @Transactional
    public ApartmentDto updateApartment(Long id, CreateApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with ID: " + id));

        apartment.setName(request.getName());
        apartment.setAddress(request.getAddress());
        apartment.setCity(request.getCity());
        apartment.setState(request.getState());
        apartment.setPincode(request.getPincode());
        apartment.setTotalUnits(request.getTotalUnits());

        Apartment updated = apartmentRepository.save(apartment);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteApartment(Long id) {
        if (!apartmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Apartment not found with ID: " + id);
        }
        apartmentRepository.deleteById(id);
    }

    public ApartmentDto mapToDto(Apartment apartment) {
        return ApartmentDto.builder()
                .id(apartment.getId())
                .name(apartment.getName())
                .address(apartment.getAddress())
                .city(apartment.getCity())
                .state(apartment.getState())
                .pincode(apartment.getPincode())
                .totalUnits(apartment.getTotalUnits())
                .createdAt(apartment.getCreatedAt())
                .build();
    }
}
