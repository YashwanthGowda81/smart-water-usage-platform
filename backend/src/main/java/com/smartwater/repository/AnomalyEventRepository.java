package com.smartwater.repository;

import com.smartwater.entity.AnomalyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyEventRepository extends JpaRepository<AnomalyEvent, Long> {
    List<AnomalyEvent> findByHouseholdIdOrderByReadingDateDesc(Long householdId);
    List<AnomalyEvent> findByStatusOrderByReadingDateDesc(String status);
    boolean existsByHouseholdIdAndReadingDateAndEventType(Long householdId, java.time.LocalDate readingDate, String eventType);
}
