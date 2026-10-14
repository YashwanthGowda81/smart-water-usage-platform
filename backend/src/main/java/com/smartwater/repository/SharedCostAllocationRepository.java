package com.smartwater.repository;

import com.smartwater.entity.SharedCostAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedCostAllocationRepository extends JpaRepository<SharedCostAllocation, Long> {
    List<SharedCostAllocation> findByBillingCycleId(Long billingCycleId);
    void deleteByBillingCycleId(Long billingCycleId);
}
