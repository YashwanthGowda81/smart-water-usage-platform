package com.smartwater.service;

import com.smartwater.dto.NotificationDto;
import com.smartwater.entity.Notification;
import com.smartwater.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDto createNotification(Long recipientId, Long apartmentId, Long householdId, String type, String title, String message) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .apartmentId(apartmentId)
                .householdId(householdId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .deliveryStatus("SENT")
                .build();

        Notification saved = notificationRepository.save(notification);
        return mapToDto(saved);
    }

    public List<NotificationDto> getUserNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long recipientId) {
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(list);
    }

    private NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .apartmentId(n.getApartmentId())
                .householdId(n.getHouseholdId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .deliveryStatus(n.getDeliveryStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
