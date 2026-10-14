package com.smartwater.repository;

import com.smartwater.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeterRepository extends JpaRepository<Meter, Long> {
    Optional<Meter> findByMeterNumber(String meterNumber);
    boolean existsByMeterNumber(String meterNumber);
    List<Meter> findByHouseholdId(Long householdId);
}
