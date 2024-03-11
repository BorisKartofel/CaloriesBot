package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @ManyToMany(mappedBy = "products")
    private List<User> users;

}
