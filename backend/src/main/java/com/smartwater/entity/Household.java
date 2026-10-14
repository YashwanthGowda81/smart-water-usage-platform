package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "households", uniqueConstraints = {
    @UniqueConstraint(name = "uq_apartment_flat", columnNames = {"apartment_id", "flat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(name = "flat_number", nullable = false, length = 50)
    private String flatNumber;

    @Column(nullable = false)
    private Integer occupancy;

    @Column(name = "area_sqft", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaSqft;

    @Column(nullable = false)
    private Boolean metered;

    @Column(nullable = false, length = 30)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
