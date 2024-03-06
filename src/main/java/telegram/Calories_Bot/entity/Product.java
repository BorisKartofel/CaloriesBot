package telegram.Calories_Bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "product_name")
    private String name;

    @Column(name = "protein_value")
    private Float protein;

    @Column(name = "fat_value")
    private Float fat;

    @Column(name = "carbohydrate_value")
    private Float carbohydrate;

    @Column(name = "kcal_value")
    private Integer kcal;

}
