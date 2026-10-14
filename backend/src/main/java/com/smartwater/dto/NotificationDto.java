package com.smartwater.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private Long recipientId;
    private Long apartmentId;
    private Long householdId;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private String deliveryStatus;
    private OffsetDateTime createdAt;
}
