package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Action;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(name = "chat_id", unique = true, nullable = false)
    private Long chatId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registeredAt;

    @OneToMany
    private Set<Notification> notifications;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_products",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    @Column(name = "current_notification_id")
    private UUID currentNotification;

    @Column(name = "current_product_uuid")
    private UUID currentProductUUID;


    public User() {
    }

    public User(Long chatId, String firstName, Action action, LocalDateTime registeredAt,
                Set<Notification> notifications, List<Product> products, UUID currentNotification,
                UUID currentProductUUID) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.action = action;
        this.registeredAt = registeredAt;
        this.notifications = notifications;
        this.products = products;
        this.currentNotification = currentNotification;
        this.currentProductUUID = currentProductUUID;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setCurrentNotification(UUID currentNotification) {
        this.currentNotification = currentNotification;
    }

    public void setCurrentProductUUID(UUID currentProductUUID) {
        this.currentProductUUID = currentProductUUID;
    }
}