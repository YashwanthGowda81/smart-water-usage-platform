package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "water_usage_logs", uniqueConstraints = {
    @UniqueConstraint(name = "uq_meter_reading_date", columnNames = {"meter_id", "reading_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "previous_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousReading;

    @Column(name = "current_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentReading;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WaterSource source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
