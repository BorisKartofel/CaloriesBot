package telegram.Calories_Bot.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.util.List;

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


    public Product() {
    }

    public Product(Integer id, String name, Float protein, Float fat, Float carbohydrate,
                   Integer kcal, List<User> users) {
        this.id = id;
        this.name = name;
        this.protein = protein;
        this.fat = fat;
        this.carbohydrate = carbohydrate;
        this.kcal = kcal;
        this.users = users;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getProtein() {
        return protein;
    }

    public void setProtein(Float protein) {
        this.protein = protein;
    }

    public Float getFat() {
        return fat;
    }

    public void setFat(Float fat) {
        this.fat = fat;
    }

    public Float getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(Float carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public Integer getKcal() {
        return kcal;
    }

    public void setKcal(Integer kcal) {
        this.kcal = kcal;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
