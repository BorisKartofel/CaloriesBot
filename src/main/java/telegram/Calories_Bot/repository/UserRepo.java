package telegram.Calories_Bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import telegram.Calories_Bot.entity.User;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    User findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.action = 'NONE' WHERE u.chatId = :chatId AND u.action = 'SENDING_PRODUCT_PERIOD'")
    void updateActionToNoneByChatId(Long chatId);

}
