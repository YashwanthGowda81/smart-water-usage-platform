package com.smartwater.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    @JsonIgnore
    private HouseholdBill bill;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType; // TARIFF_CHARGE, SHARED_COST, SHARED_AREA, ADJUSTMENT

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
}
