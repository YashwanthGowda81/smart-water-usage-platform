package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bulk_water_purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkWaterPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apartment_id", nullable = false)
    private Long apartmentId;

    @Column(name = "billing_cycle_id")
    private Long billingCycleId;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false, length = 150)
    private String supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_type", nullable = false, length = 50)
    @Builder.Default
    private PurchaseType purchaseType = PurchaseType.TANKER;

    @Column(name = "volume_kl", nullable = false, precision = 12, scale = 2)
    private BigDecimal volumeKl;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_id")
    private Long createdById;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
