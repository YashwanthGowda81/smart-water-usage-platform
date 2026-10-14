package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "household_bills", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cycle_household", columnNames = {"billing_cycle_id", "household_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "billing_cycle_id", nullable = false)
    private Long billingCycleId;

    @Column(name = "household_id", nullable = false)
    private Long householdId;

    @Column(name = "metered_consumption_kl", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal meteredConsumptionKl = BigDecimal.ZERO;

    @Column(name = "progressive_tariff_charge", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal progressiveTariffCharge = BigDecimal.ZERO;

    @Column(name = "shared_cost_allocation", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sharedCostAllocation = BigDecimal.ZERO;

    @Column(name = "shared_area_allocation", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sharedAreaAllocation = BigDecimal.ZERO;

    @Column(name = "adjustments", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal adjustments = BigDecimal.ZERO;

    @Column(name = "total_payable", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPayable = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BillStatus status = BillStatus.UNPAID;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<BillItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
