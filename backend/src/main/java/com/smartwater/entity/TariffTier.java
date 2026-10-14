package com.smartwater.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_plan_id", nullable = false)
    @JsonIgnore
    private TariffPlan tariffPlan;

    @Column(name = "tier_number", nullable = false)
    private Integer tierNumber;

    @Column(name = "min_kl", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minKl = BigDecimal.ZERO;

    @Column(name = "max_kl", precision = 10, scale = 2)
    private BigDecimal maxKl; // null means upper tier bound (unlimited)

    @Column(name = "rate_per_kl", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerKl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
