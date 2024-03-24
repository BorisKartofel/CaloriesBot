package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import telegram.Calories_Bot.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Integer> {

    List<Product> findAllByNameContainingIgnoreCase(String productSubname);

    Optional<Product> findProductByName(String productName);

}
