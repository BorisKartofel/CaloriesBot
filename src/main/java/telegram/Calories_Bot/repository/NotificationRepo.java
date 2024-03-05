package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegram.Calories_Bot.entity.Notification;

import java.util.UUID;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, UUID> {
}
