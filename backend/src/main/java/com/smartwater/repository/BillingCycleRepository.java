package com.smartwater.repository;

import com.smartwater.entity.BillingCycle;
import com.smartwater.entity.BillingCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingCycleRepository extends JpaRepository<BillingCycle, Long> {
    List<BillingCycle> findByApartmentIdOrderByPeriodStartDesc(Long apartmentId);
    Optional<BillingCycle> findByApartmentIdAndPeriodStartAndPeriodEnd(Long apartmentId, LocalDate periodStart, LocalDate periodEnd);
    List<BillingCycle> findByApartmentIdAndStatus(Long apartmentId, BillingCycleStatus status);
}
