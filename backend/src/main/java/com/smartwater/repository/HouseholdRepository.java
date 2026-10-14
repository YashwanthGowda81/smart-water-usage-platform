package com.smartwater.repository;

import com.smartwater.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    List<Household> findByApartmentId(Long apartmentId);
    boolean existsByApartmentIdAndFlatNumber(Long apartmentId, String flatNumber);
    Optional<Household> findByApartmentIdAndFlatNumber(Long apartmentId, String flatNumber);
}
