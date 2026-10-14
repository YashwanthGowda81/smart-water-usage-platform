package com.smartwater.repository;

import com.smartwater.entity.BulkWaterPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BulkWaterPurchaseRepository extends JpaRepository<BulkWaterPurchase, Long> {
    List<BulkWaterPurchase> findByApartmentIdOrderByPurchaseDateDesc(Long apartmentId);
    List<BulkWaterPurchase> findByBillingCycleId(Long billingCycleId);
    List<BulkWaterPurchase> findByApartmentIdAndPurchaseDateBetween(Long apartmentId, LocalDate startDate, LocalDate endDate);
}
