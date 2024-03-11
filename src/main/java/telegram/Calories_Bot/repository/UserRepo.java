package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import telegram.Calories_Bot.entity.Product;
import telegram.Calories_Bot.entity.User;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    User findByChatId(Long chatId);
    boolean existsByChatId(Long chatId);
}
