package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import telegram.Calories_Bot.entity.Product;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Integer> {

    List<Product> findFirst16ByNameContainingIgnoreCase(String productSubname);

}
