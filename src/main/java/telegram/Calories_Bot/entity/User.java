package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Action;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_products",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    List<Product> products;

    @Column(name = "current_notification_id")
    UUID currentNotification;

    @Column(name = "current_product_uuid")
    UUID currentProductUUID;


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

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public UUID getCurrentNotification() {
        return currentNotification;
    }

    public void setCurrentNotification(UUID currentNotification) {
        this.currentNotification = currentNotification;
    }

    public UUID getCurrentProductUUID() {
        return currentProductUUID;
    }

    public void setCurrentProductUUID(UUID currentProductUUID) {
        this.currentProductUUID = currentProductUUID;
    }
}