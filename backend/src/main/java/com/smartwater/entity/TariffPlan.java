package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tariff_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "effective_start_date", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal baseRate = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "tariffPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("tierNumber ASC")
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
