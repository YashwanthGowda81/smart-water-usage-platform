package com.smartwater.controller;

import com.smartwater.dto.NotificationDto;
import com.smartwater.entity.User;
import com.smartwater.exception.ResourceNotFoundException;
import com.smartwater.repository.UserRepository;
import com.smartwater.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for In-App User Alerts & Notification Center")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping({"", "/my-notifications"})
    @Operation(summary = "Get all notifications for the currently authenticated user")
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<NotificationDto> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Mark a notification as read (supports PUT and POST)")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/read-all", method = {RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Mark all notifications for the current user as read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}

