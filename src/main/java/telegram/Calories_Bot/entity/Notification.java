package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Status;

@Getter
@Builder
@Entity
@Table(name = "notifications")
public class Notification extends AbstractEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "seconds")
    private Integer seconds;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public Notification() {
    }

    public Notification(String title, String description, Status status, Integer seconds, User user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.seconds = seconds;
        this.user = user;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    public void setUser(User user) {
        this.user = user;
    }

}

