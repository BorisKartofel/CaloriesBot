package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Status;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@Getter
@Builder
@Entity
@Table(name = "users_products")
public class UserProduct extends AbstractEntity {


    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_grams")
    private Integer productGrams;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "eating_time")
    private LocalDateTime eatingTime;


    public UserProduct() {
    }

    public UserProduct(UUID userId, Integer productId, Integer productGrams, Status status, LocalDateTime eatingTime) {
        this.userId = userId;
        this.productId = productId;
        this.productGrams = productGrams;
        this.status = status;
        this.eatingTime = eatingTime;
    }


    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public void setProductGrams(Integer productGrams) {
        this.productGrams = productGrams;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProduct that = (UserProduct) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(productId, that.productId)
                && Objects.equals(productGrams, that.productGrams)
                && status == that.status
                && Objects.equals(eatingTime, that.eatingTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId, productGrams, status, eatingTime);
    }
}
