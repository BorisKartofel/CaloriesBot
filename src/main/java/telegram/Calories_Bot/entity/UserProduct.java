package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import telegram.Calories_Bot.entity.contract.AbstractEntity;
import telegram.Calories_Bot.entity.enums.Status;

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


    public UserProduct() {
    }

    public UserProduct(UUID userId, Integer productId, Integer productGrams, Status status) {
        this.userId = userId;
        this.productId = productId;
        this.productGrams = productGrams;
        this.status = status;
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

}
