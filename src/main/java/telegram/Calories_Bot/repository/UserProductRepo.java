package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import telegram.Calories_Bot.entity.UserProduct;
import telegram.Calories_Bot.entity.enums.Status;

import java.util.List;
import java.util.UUID;

public interface UserProductRepo extends JpaRepository<UserProduct, UUID> {

    List<UserProduct> findUserProductsByUserId(UUID uuid);

    @Transactional
    void deleteUserProductByUserIdAndStatus(UUID userId, Status status);

}
