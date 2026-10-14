package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "billing_cycles", uniqueConstraints = {
    @UniqueConstraint(name = "uq_apartment_billing_period", columnNames = {"apartment_id", "period_start", "period_end"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BillingCycleStatus status = BillingCycleStatus.OPEN;

    @Column(name = "total_water_procured_cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalWaterProcuredCost = BigDecimal.ZERO;

    @Column(name = "total_consumption_kl", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalConsumptionKl = BigDecimal.ZERO;

    @Column(name = "tariff_snapshot_json", columnDefinition = "TEXT")
    private String tariffSnapshotJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
