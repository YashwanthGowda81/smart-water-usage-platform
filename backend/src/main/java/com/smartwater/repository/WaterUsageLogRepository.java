package com.smartwater.repository;

import com.smartwater.entity.WaterUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterUsageLogRepository extends JpaRepository<WaterUsageLog, Long> {
    List<WaterUsageLog> findByMeterIdOrderByReadingDateDesc(Long meterId);
    boolean existsByMeterIdAndReadingDate(Long meterId, LocalDate readingDate);
    Optional<WaterUsageLog> findByMeterIdAndReadingDate(Long meterId, LocalDate readingDate);

    @Query("SELECT u FROM WaterUsageLog u WHERE u.meter.id = :meterId AND u.readingDate < :readingDate ORDER BY u.readingDate DESC")
    List<WaterUsageLog> findPreviousReadings(@Param("meterId") Long meterId, @Param("readingDate") LocalDate readingDate);

    @Query("SELECT u FROM WaterUsageLog u WHERE u.meter.id = :meterId ORDER BY u.readingDate DESC")
    List<WaterUsageLog> findLatestReading(@Param("meterId") Long meterId);

    @Query("SELECT SUM(u.consumption) FROM WaterUsageLog u WHERE u.meter.household.id = :householdId AND u.readingDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateConsumptionByHouseholdAndDateRange(@Param("householdId") Long householdId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT u FROM WaterUsageLog u WHERE u.meter.household.id = :householdId ORDER BY u.readingDate DESC")
    List<WaterUsageLog> findByHouseholdIdOrderByReadingDateDesc(@Param("householdId") Long householdId);
}
