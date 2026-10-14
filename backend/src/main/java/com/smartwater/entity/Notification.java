package com.smartwater.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "apartment_id")
    private Long apartmentId;

    @Column(name = "household_id")
    private Long householdId;

    @Column(nullable = false, length = 50)
    private String type; // BILL_NOTIFICATION, ANOMALY_ALERT, GENERAL

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "delivery_status", nullable = false, length = 30)
    @Builder.Default
    private String deliveryStatus = "SENT";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
