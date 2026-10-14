package com.smartwater.service;

import com.smartwater.dto.CreateWaterUsageRequest;
import com.smartwater.dto.WaterUsageLogDto;
import com.smartwater.entity.Meter;
import com.smartwater.entity.WaterSource;
import com.smartwater.entity.WaterUsageLog;
import com.smartwater.exception.DuplicateResourceException;
import com.smartwater.exception.InvalidReadingException;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.MeterRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterUsageService {

    private final WaterUsageLogRepository waterUsageLogRepository;
    private final MeterRepository meterRepository;

    @Transactional
    public WaterUsageLogDto recordWaterUsage(CreateWaterUsageRequest request) {
        Meter meter = meterRepository.findByMeterNumber(request.getMeterNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with number: " + request.getMeterNumber()));

        if (waterUsageLogRepository.existsByMeterIdAndReadingDate(meter.getId(), request.getReadingDate())) {
            throw new DuplicateResourceException(
                    String.format("Reading already exists for meter '%s' on date %s", request.getMeterNumber(), request.getReadingDate())
            );
        }

        if (request.getCurrentReading().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidReadingException("Current reading cannot be negative: " + request.getCurrentReading());
        }

        // Determine previous reading: find latest log before readingDate
        List<WaterUsageLog> previousLogs = waterUsageLogRepository.findPreviousReadings(meter.getId(), request.getReadingDate());
        BigDecimal previousReading = BigDecimal.ZERO;
        if (!previousLogs.isEmpty()) {
            previousReading = previousLogs.get(0).getCurrentReading();
        }

        if (request.getCurrentReading().compareTo(previousReading) < 0) {
            throw new InvalidReadingException(
                    String.format("Current reading (%s) cannot be lower than previous reading (%s)",
                            request.getCurrentReading(), previousReading)
            );
        }

        // Bidirectional continuity check: current reading cannot exceed next recorded reading
        List<WaterUsageLog> nextLogs = waterUsageLogRepository.findNextReadings(meter.getId(), request.getReadingDate());
        if (!nextLogs.isEmpty()) {
            BigDecimal nextReading = nextLogs.get(0).getCurrentReading();
            if (request.getCurrentReading().compareTo(nextReading) > 0) {
                throw new InvalidReadingException(
                        String.format("Current reading (%s) cannot be higher than subsequent reading (%s) on %s",
                                request.getCurrentReading(), nextReading, nextLogs.get(0).getReadingDate())
                );
            }
        }

        BigDecimal consumption = request.getCurrentReading().subtract(previousReading);
        WaterSource source = request.getSource() != null ? request.getSource() : WaterSource.MANUAL;

        WaterUsageLog log = WaterUsageLog.builder()
                .meter(meter)
                .readingDate(request.getReadingDate())
                .previousReading(previousReading)
                .currentReading(request.getCurrentReading())
                .consumption(consumption)
                .source(source)
                .build();

        WaterUsageLog saved = waterUsageLogRepository.save(log);

        // If a subsequent reading exists, update its previousReading and consumption to maintain continuity
        if (!nextLogs.isEmpty()) {
            WaterUsageLog nextLog = nextLogs.get(0);
            nextLog.setPreviousReading(request.getCurrentReading());
            nextLog.setConsumption(nextLog.getCurrentReading().subtract(request.getCurrentReading()));
            waterUsageLogRepository.save(nextLog);
        }

        return mapToDto(saved);
    }

    public List<WaterUsageLogDto> getUsageByMeter(String meterNumber) {
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with number: " + meterNumber));

        return waterUsageLogRepository.findByMeterIdOrderByReadingDateDesc(meter.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<WaterUsageLogDto> getUsageByHousehold(Long householdId) {
        return waterUsageLogRepository.findByHouseholdIdOrderByReadingDateDesc(householdId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<WaterUsageLogDto> getAllUsageLogs() {
        return waterUsageLogRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public WaterUsageLogDto mapToDto(WaterUsageLog log) {
        return WaterUsageLogDto.builder()
                .id(log.getId())
                .meterId(log.getMeter().getId())
                .meterNumber(log.getMeter().getMeterNumber())
                .flatNumber(log.getMeter().getHousehold().getFlatNumber())
                .apartmentName(log.getMeter().getHousehold().getApartment().getName())
                .readingDate(log.getReadingDate())
                .previousReading(log.getPreviousReading())
                .currentReading(log.getCurrentReading())
                .consumption(log.getConsumption())
                .source(log.getSource())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
