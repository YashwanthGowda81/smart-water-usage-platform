package com.smartwater.repository;

import com.smartwater.entity.HouseholdBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdBillRepository extends JpaRepository<HouseholdBill, Long> {
    List<HouseholdBill> findByBillingCycleId(Long billingCycleId);
    List<HouseholdBill> findByHouseholdIdOrderByCreatedAtDesc(Long householdId);
    Optional<HouseholdBill> findByBillingCycleIdAndHouseholdId(Long billingCycleId, Long householdId);
    void deleteByBillingCycleId(Long billingCycleId);
}
