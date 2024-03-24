package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Action;
import telegram.Calories_Bot.entity.enums.Status;

import java.util.UUID;


@Builder
@Entity
@Table(name = "users_products")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProduct extends AbstractEntity {


    @Column(name = "user_id")
    UUID userId;

    @Column(name = "product_id")
    Integer productId;

    @Column(name = "product_grams")
    Integer productGrams;

    @Enumerated(EnumType.STRING)
    Status status;



    public UserProduct() {
    }

    public UserProduct(UUID userId, Integer productId, Integer productGrams, Status status) {
        this.userId = userId;
        this.productId = productId;
        this.productGrams = productGrams;
        this.status = status;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getProductGrams() {
        return productGrams;
    }

    public void setProductGrams(Integer productGrams) {
        this.productGrams = productGrams;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
