package com.smartwater.service;

import com.smartwater.dto.CreateMeterRequest;
import com.smartwater.dto.MeterDto;
import com.smartwater.entity.Household;
import com.smartwater.entity.Meter;
import com.smartwater.entity.MeterStatus;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final HouseholdRepository householdRepository;

    @Transactional
    public MeterDto createMeter(CreateMeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already registered: " + request.getMeterNumber());
        }

        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with ID: " + request.getHouseholdId()));

        MeterStatus status = request.getStatus() != null ? request.getStatus() : MeterStatus.ACTIVE;

        Meter meter = Meter.builder()
                .household(household)
                .meterNumber(request.getMeterNumber())
                .meterType(request.getMeterType())
                .installationDate(request.getInstallationDate())
                .status(status)
                .build();

        Meter saved = meterRepository.save(meter);
        return mapToDto(saved);
    }

    public List<MeterDto> getAllMeters() {
        return meterRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public MeterDto getMeterById(Long id) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with ID: " + id));
        return mapToDto(meter);
    }

    public MeterDto getMeterByMeterNumber(String meterNumber) {
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with number: " + meterNumber));
        return mapToDto(meter);
    }

    public List<MeterDto> getMetersByHousehold(Long householdId) {
        return meterRepository.findByHouseholdId(householdId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MeterDto updateMeter(Long id, CreateMeterRequest request) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with ID: " + id));

        if (!meter.getMeterNumber().equals(request.getMeterNumber()) && meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already registered: " + request.getMeterNumber());
        }

        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with ID: " + request.getHouseholdId()));

        meter.setHousehold(household);
        meter.setMeterNumber(request.getMeterNumber());
        meter.setMeterType(request.getMeterType());
        meter.setInstallationDate(request.getInstallationDate());
        if (request.getStatus() != null) {
            meter.setStatus(request.getStatus());
        }

        Meter updated = meterRepository.save(meter);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteMeter(Long id) {
        if (!meterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meter not found with ID: " + id);
        }
        meterRepository.deleteById(id);
    }

    public MeterDto mapToDto(Meter meter) {
        return MeterDto.builder()
                .id(meter.getId())
                .householdId(meter.getHousehold().getId())
                .flatNumber(meter.getHousehold().getFlatNumber())
                .apartmentName(meter.getHousehold().getApartment().getName())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .createdAt(meter.getCreatedAt())
                .build();
    }
}
