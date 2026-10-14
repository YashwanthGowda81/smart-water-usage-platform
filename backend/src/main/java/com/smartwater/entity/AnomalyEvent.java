package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "anomaly_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meter_id")
    private Long meterId;

    @Column(name = "household_id", nullable = false)
    private Long householdId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // THRESHOLD_EXCEEDED, POTENTIAL_LEAK

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "observed_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal observedValue;

    @Column(name = "baseline_mean", precision = 12, scale = 2)
    private BigDecimal baselineMean;

    @Column(name = "baseline_stddev", precision = 12, scale = 2)
    private BigDecimal baselineStddev;

    @Column(name = "threshold_value", precision = 12, scale = 2)
    private BigDecimal thresholdValue;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN"; // OPEN, RESOLVED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
