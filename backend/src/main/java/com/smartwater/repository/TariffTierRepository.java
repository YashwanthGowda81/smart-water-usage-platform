package com.smartwater.repository;

import com.smartwater.entity.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, Long> {
    List<TariffTier> findByTariffPlanIdOrderByTierNumberAsc(Long tariffPlanId);
}
