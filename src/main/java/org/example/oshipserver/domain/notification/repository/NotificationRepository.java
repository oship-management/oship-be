package org.example.oshipserver.domain.notification.repository;

import org.example.oshipserver.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}