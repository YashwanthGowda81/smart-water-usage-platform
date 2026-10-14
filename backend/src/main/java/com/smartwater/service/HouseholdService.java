package com.smartwater.service;

import com.smartwater.dto.CreateHouseholdRequest;
import com.smartwater.dto.HouseholdDto;
import com.smartwater.entity.Apartment;
import com.smartwater.entity.Household;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.ApartmentRepository;
import com.smartwater.repository.HouseholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final ApartmentRepository apartmentRepository;

    @Transactional
    public HouseholdDto createHousehold(CreateHouseholdRequest request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with ID: " + request.getApartmentId()));

        if (householdRepository.existsByApartmentIdAndFlatNumber(request.getApartmentId(), request.getFlatNumber())) {
            throw new DuplicateResourceException(
                    String.format("Flat number '%s' already exists in apartment '%s'", request.getFlatNumber(), apartment.getName())
            );
        }

        String status = request.getStatus() != null ? request.getStatus() : "ACTIVE";

        Household household = Household.builder()
                .apartment(apartment)
                .flatNumber(request.getFlatNumber())
                .occupancy(request.getOccupancy())
                .areaSqft(request.getAreaSqft())
                .metered(request.getMetered())
                .status(status)
                .build();

        Household saved = householdRepository.save(household);
        return mapToDto(saved);
    }

    public List<HouseholdDto> getAllHouseholds() {
        return householdRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<HouseholdDto> getHouseholdsByApartment(Long apartmentId) {
        return householdRepository.findByApartmentId(apartmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public HouseholdDto getHouseholdById(Long id) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with ID: " + id));
        return mapToDto(household);
    }

    @Transactional
    public HouseholdDto updateHousehold(Long id, CreateHouseholdRequest request) {
        Household household = householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with ID: " + id));

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with ID: " + request.getApartmentId()));

        // Check if flat number is changing and duplicate exists
        if (!household.getFlatNumber().equals(request.getFlatNumber()) || !household.getApartment().getId().equals(request.getApartmentId())) {
            if (householdRepository.existsByApartmentIdAndFlatNumber(request.getApartmentId(), request.getFlatNumber())) {
                throw new DuplicateResourceException(
                        String.format("Flat number '%s' already exists in apartment '%s'", request.getFlatNumber(), apartment.getName())
                );
            }
        }

        household.setApartment(apartment);
        household.setFlatNumber(request.getFlatNumber());
        household.setOccupancy(request.getOccupancy());
        household.setAreaSqft(request.getAreaSqft());
        household.setMetered(request.getMetered());
        if (request.getStatus() != null) {
            household.setStatus(request.getStatus());
        }

        Household updated = householdRepository.save(household);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteHousehold(Long id) {
        if (!householdRepository.existsById(id)) {
            throw new ResourceNotFoundException("Household not found with ID: " + id);
        }
        householdRepository.deleteById(id);
    }

    public HouseholdDto mapToDto(Household household) {
        return HouseholdDto.builder()
                .id(household.getId())
                .apartmentId(household.getApartment().getId())
                .apartmentName(household.getApartment().getName())
                .flatNumber(household.getFlatNumber())
                .occupancy(household.getOccupancy())
                .areaSqft(household.getAreaSqft())
                .metered(household.getMetered())
                .status(household.getStatus())
                .createdAt(household.getCreatedAt())
                .build();
    }
}
