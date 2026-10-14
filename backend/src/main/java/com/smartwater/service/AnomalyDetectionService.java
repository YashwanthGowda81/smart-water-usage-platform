package com.smartwater.service;

import com.smartwater.dto.AnomalyEventDto;
import com.smartwater.entity.AnomalyEvent;
import com.smartwater.entity.Household;
import com.smartwater.entity.User;
import com.smartwater.entity.WaterUsageLog;
import com.smartwater.repository.AnomalyEventRepository;
import com.smartwater.repository.HouseholdRepository;
import com.smartwater.repository.UserRepository;
import com.smartwater.repository.WaterUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final AnomalyEventRepository anomalyEventRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public List<AnomalyEventDto> evaluateHouseholdAnomalies(Long householdId) {
        List<WaterUsageLog> logs = waterUsageLogRepository.findByHouseholdIdOrderByReadingDateDesc(householdId);

        if (logs.size() < 3) {
            return getAnomaliesByHousehold(householdId); // Need at least 3 historical readings for standard deviation
        }

        WaterUsageLog latest = logs.get(0);
        List<WaterUsageLog> history = logs.subList(1, Math.min(logs.size(), 30));

        double sum = 0.0;
        for (WaterUsageLog log : history) {
            sum += log.getConsumption().doubleValue();
        }
        double mean = sum / history.size();

        double varianceSum = 0.0;
        for (WaterUsageLog log : history) {
            varianceSum += Math.pow(log.getConsumption().doubleValue() - mean, 2);
        }
        double stddev = Math.sqrt(varianceSum / history.size());

        double threshold = mean + (2 * stddev);
        double latestValue = latest.getConsumption().doubleValue();

        if (latestValue > threshold && latestValue > 5.0) { // Require minimum 5 kL threshold to avoid noise
            boolean alreadyExists = anomalyEventRepository.existsByHouseholdIdAndReadingDateAndEventType(
                    householdId, latest.getReadingDate(), "POTENTIAL_LEAK"
            );

            if (!alreadyExists) {
                AnomalyEvent event = AnomalyEvent.builder()
                        .meterId(latest.getMeter() != null ? latest.getMeter().getId() : null)
                        .householdId(householdId)
                        .eventType("POTENTIAL_LEAK")
                        .readingDate(latest.getReadingDate())
                        .observedValue(BigDecimal.valueOf(latestValue).setScale(2, RoundingMode.HALF_UP))
                        .baselineMean(BigDecimal.valueOf(mean).setScale(2, RoundingMode.HALF_UP))
                        .baselineStddev(BigDecimal.valueOf(stddev).setScale(2, RoundingMode.HALF_UP))
                        .thresholdValue(BigDecimal.valueOf(threshold).setScale(2, RoundingMode.HALF_UP))
                        .status("OPEN")
                        .build();

                anomalyEventRepository.save(event);

                // Notify resident user associated with this household
                List<User> residents = userRepository.findByHouseholdId(householdId);
                Household hh = householdRepository.findById(householdId).orElse(null);
                String flatNum = hh != null ? hh.getFlatNumber() : "Unit";

                for (User r : residents) {
                    notificationService.createNotification(
                            r.getId(),
                            hh != null && hh.getApartment() != null ? hh.getApartment().getId() : null,
                            householdId,
                            "ANOMALY_ALERT",
                            "⚠️ Potential Water Leak Alert - Flat " + flatNum,
                            String.format("Abnormal water consumption of %.2f kL observed on %s. Historical baseline: %.2f kL (Threshold: %.2f kL). Please check plumbing for leaks.",
                                    latestValue, latest.getReadingDate(), mean, threshold)
                    );
                }
            }
        }

        return getAnomaliesByHousehold(householdId);
    }

    @Transactional
    public AnomalyEventDto updateAnomalyStatus(Long anomalyId, String status) {
        AnomalyEvent event = anomalyEventRepository.findById(anomalyId)
                .orElseThrow(() -> new com.smartwater.exception.ResourceNotFoundException("Anomaly event not found with ID: " + anomalyId));
        event.setStatus(status != null ? status.trim().toUpperCase() : "RESOLVED");
        AnomalyEvent updated = anomalyEventRepository.save(event);
        return mapToDto(updated);
    }

    public List<AnomalyEventDto> getAnomaliesByHousehold(Long householdId) {
        return anomalyEventRepository.findByHouseholdIdOrderByReadingDateDesc(householdId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AnomalyEventDto> getAllOpenAnomalies() {
        return anomalyEventRepository.findByStatusOrderByReadingDateDesc("OPEN").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1:00 AM
    public void runDailyScheduledAnomalyScan() {
        List<Household> households = householdRepository.findAll();
        for (Household hh : households) {
            try {
                evaluateHouseholdAnomalies(hh.getId());
            } catch (Exception e) {
                // Log and continue to next household
            }
        }
    }

    private AnomalyEventDto mapToDto(AnomalyEvent a) {
        Household hh = householdRepository.findById(a.getHouseholdId()).orElse(null);
        String flatNum = hh != null ? hh.getFlatNumber() : "N/A";
        String apName = "Apartment";

        return AnomalyEventDto.builder()
                .id(a.getId())
                .meterId(a.getMeterId())
                .householdId(a.getHouseholdId())
                .flatNumber(flatNum)
                .apartmentName(apName)
                .eventType(a.getEventType())
                .readingDate(a.getReadingDate())
                .observedValue(a.getObservedValue())
                .baselineMean(a.getBaselineMean())
                .baselineStddev(a.getBaselineStddev())
                .thresholdValue(a.getThresholdValue())
                .status(a.getStatus())
                .build();
    }
}
