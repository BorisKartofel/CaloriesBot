package telegram.Calories_Bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users_products")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProduct {

    @Id
    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "product_id", nullable = false)
    Integer productId;



}
