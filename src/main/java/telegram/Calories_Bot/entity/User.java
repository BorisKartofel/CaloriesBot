package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import telegram.Calories_Bot.entity.contract.AbstractEntity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AbstractEntity {

    @Column(name = "chat_id", unique = true, nullable = false)
    Long chatId;

    @Column(name = "first_name", nullable = false)
    String firstName;

    @Enumerated(EnumType.STRING)
    Action action;

    @Column(name = "registration_date", nullable = false)
    LocalDateTime registeredAt;

    @OneToMany
    Set<Notification> notifications;

    @Column(name = "current_notification_id")
    UUID currentNotification;

}