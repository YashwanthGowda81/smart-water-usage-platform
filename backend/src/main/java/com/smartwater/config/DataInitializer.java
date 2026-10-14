package com.smartwater.config;

import com.smartwater.entity.*;
import com.smartwater.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Disabled for strict production mode (No dummy data seeding)
// @Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final MeterRepository meterRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking PostgreSQL database for demo data initialization...");

        // 1. Initialize Users (upsert to ensure passwords match demo credentials)
        User admin = userRepository.findByEmail("admin@smartwater.com")
                .orElseGet(() -> User.builder().email("admin@smartwater.com").build());
        admin.setName("Smart Water Admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        log.info("Ensured Admin user: admin@smartwater.com / admin123");

        User resident = userRepository.findByEmail("rahul@smartwater.com")
                .orElseGet(() -> User.builder().email("rahul@smartwater.com").build());
        resident.setName("Rahul Kumar");
        resident.setPasswordHash(passwordEncoder.encode("rahul123"));
        resident.setRole(Role.RESIDENT);
        resident.setStatus(UserStatus.ACTIVE);
        userRepository.save(resident);
        log.info("Ensured Resident user: rahul@smartwater.com / rahul123");

        // 2. Initialize Apartment
        Apartment apartment = apartmentRepository.findAll().stream()
                .filter(a -> "Green Valley Residency".equalsIgnoreCase(a.getName()))
                .findFirst()
                .orElseGet(() -> apartmentRepository.save(Apartment.builder()
                        .name("Green Valley Residency")
                        .address("25 Green Valley Road")
                        .city("Bengaluru")
                        .state("Karnataka")
                        .pincode("560060")
                        .totalUnits(4)
                        .build()));

        // 3. Initialize Households
        Household h101 = getOrCreateHousehold(apartment, "A-101", 3, new BigDecimal("1200.00"), true);
        Household h102 = getOrCreateHousehold(apartment, "A-102", 4, new BigDecimal("1350.00"), true);
        Household h103 = getOrCreateHousehold(apartment, "A-103", 2, new BigDecimal("950.00"), true);
        Household h104 = getOrCreateHousehold(apartment, "A-104", 5, new BigDecimal("1500.00"), true);

        // 4. Initialize Meters
        Meter m101 = getOrCreateMeter(h101, "MTR-A101-001", MeterType.DIGITAL);
        Meter m102 = getOrCreateMeter(h102, "MTR-A102-001", MeterType.DIGITAL);
        Meter m103 = getOrCreateMeter(h103, "MTR-A103-001", MeterType.ANALOG);
        Meter m104 = getOrCreateMeter(h104, "MTR-A104-001", MeterType.DIGITAL);

        // 5. Initialize Water Readings
        seedMeterReadings(m101, new double[]{8.5, 9.2, 7.8, 10.1, 8.9});
        seedMeterReadings(m102, new double[]{11.2, 12.4, 10.8, 13.1, 11.7});
        seedMeterReadings(m103, new double[]{5.7, 6.2, 5.1, 6.8, 5.9});
        seedMeterReadings(m104, new double[]{14.8, 15.2, 13.9, 16.1, 14.5});

        log.info("PostgreSQL demo data initialization completed successfully.");
    }

    private Household getOrCreateHousehold(Apartment apartment, String flatNumber, int occupancy, BigDecimal areaSqft, boolean metered) {
        return householdRepository.findByApartmentIdAndFlatNumber(apartment.getId(), flatNumber)
                .orElseGet(() -> householdRepository.save(Household.builder()
                        .apartment(apartment)
                        .flatNumber(flatNumber)
                        .occupancy(occupancy)
                        .areaSqft(areaSqft)
                        .metered(metered)
                        .status("ACTIVE")
                        .build()));
    }

    private Meter getOrCreateMeter(Household household, String meterNumber, MeterType type) {
        return meterRepository.findByMeterNumber(meterNumber)
                .orElseGet(() -> meterRepository.save(Meter.builder()
                        .household(household)
                        .meterNumber(meterNumber)
                        .meterType(type)
                        .installationDate(LocalDate.of(2026, 1, 1))
                        .status(MeterStatus.ACTIVE)
                        .build()));
    }

    private void seedMeterReadings(Meter meter, double[] incrementalConsumptions) {
        List<LocalDate> dates = List.of(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 9, 29)
        );

        BigDecimal cumulativeReading = BigDecimal.ZERO;

        for (int i = 0; i < dates.size() && i < incrementalConsumptions.length; i++) {
            LocalDate date = dates.get(i);
            BigDecimal consumption = BigDecimal.valueOf(incrementalConsumptions[i]);
            BigDecimal prevReading = cumulativeReading;
            cumulativeReading = cumulativeReading.add(consumption);

            if (!waterUsageLogRepository.existsByMeterIdAndReadingDate(meter.getId(), date)) {
                waterUsageLogRepository.save(WaterUsageLog.builder()
                        .meter(meter)
                        .readingDate(date)
                        .previousReading(prevReading)
                        .currentReading(cumulativeReading)
                        .consumption(consumption)
                        .source(WaterSource.SIMULATED)
                        .build());
            }
        }
    }
}
