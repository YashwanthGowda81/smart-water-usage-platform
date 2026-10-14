package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "shared_cost_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedCostAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "billing_cycle_id", nullable = false)
    private Long billingCycleId;

    @Column(name = "household_id", nullable = false)
    private Long householdId;

    @Column(name = "allocation_basis", nullable = false, length = 50)
    private String allocationBasis; // CONSUMPTION_PROPORTIONAL, AREA_FALLBACK

    @Column(name = "allocation_percentage", nullable = false, precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal allocationPercentage = BigDecimal.ZERO;

    @Column(name = "allocated_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "calculation_inputs_json", columnDefinition = "TEXT")
    private String calculationInputsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
