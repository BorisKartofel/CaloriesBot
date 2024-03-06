package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import telegram.Calories_Bot.entity.UserProduct;

import java.util.List;
import java.util.UUID;

public interface UserProductRepository extends JpaRepository<UserProduct, UUID> {

    List<UserProduct> findUserProductsByUserId(UUID userId);

}
