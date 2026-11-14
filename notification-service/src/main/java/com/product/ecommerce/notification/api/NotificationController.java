package com.product.ecommerce.notification.api;

import com.product.ecommerce.common.response.ApiResponse;
import com.product.ecommerce.notification.service.NotificationService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
  private final NotificationService notifications;

  public NotificationController(NotificationService notifications) {
    this.notifications = notifications;
  }

  @GetMapping
  public ApiResponse<List<NotificationResponse>> list(Principal principal) {
    return ApiResponse.success("Notifications retrieved",
        notifications.listForUser(UUID.fromString(principal.getName())));
  }

  @PutMapping("/{notificationId}/read")
  public ApiResponse<NotificationResponse> markRead(Principal principal,
      @PathVariable UUID notificationId) {
    return ApiResponse.success("Notification marked as read",
        notifications.markRead(UUID.fromString(principal.getName()), notificationId));
  }
}
