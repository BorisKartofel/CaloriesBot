package telegram.Calories_Bot.service.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.repository.UserRepo;

@Service
public class ProductManager {

    private final UserRepo userRepo;


    @Autowired
    public ProductManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        //TODO Добавить логику удаления прошлого сообщения

        User user = userRepo.findByChatId(message.getChatId());

        switch(user.getAction()) {
            case SENDING_PRODUCT -> {
                // TODO Добавить логику
                return null;
            }
        }
        return null;
    }
}
