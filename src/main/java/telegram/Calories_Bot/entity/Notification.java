package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Status;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification extends AbstractEntity {

    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "seconds")
    Integer seconds;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

}

