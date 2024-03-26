package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import telegram.Calories_Bot.entity.Notification;
import telegram.Calories_Bot.entity.enums.Status;

import java.util.UUID;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, UUID> {

    @Transactional
    void deleteNotificationByUserIdAndStatus(UUID userId, Status status);

}
