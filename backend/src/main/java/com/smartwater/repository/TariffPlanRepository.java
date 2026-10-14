package com.smartwater.repository;

import com.smartwater.entity.TariffPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TariffPlanRepository extends JpaRepository<TariffPlan, Long> {
    List<TariffPlan> findByApartmentId(Long apartmentId);
    Optional<TariffPlan> findFirstByApartmentIdAndActiveTrueAndEffectiveStartDateLessThanEqualOrderByEffectiveStartDateDesc(
            Long apartmentId, LocalDate date
    );
}
