package telegram.Calories_Bot.service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.contract.AbstractHandler;
import telegram.Calories_Bot.service.manager.MainManager;
import telegram.Calories_Bot.service.manager.NotificationManager;
import telegram.Calories_Bot.service.manager.ProductManager;

@Service
public class MessageHandler extends AbstractHandler {
    private final UserRepo userRepo;
    private final NotificationManager notificationManager;
    private final MainManager mainManager;
    private final ProductManager productManager;

    public MessageHandler(UserRepo userRepo, NotificationManager notificationManager, MainManager mainManager, ProductManager productManager) {
        this.userRepo = userRepo;
        this.notificationManager = notificationManager;
        this.mainManager = mainManager;
        this.productManager = productManager;
    }

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException {
        Message message = (Message) object;
        User user = userRepo.findByChatId(message.getChatId());
        switch (user.getAction()) {
            case NONE -> {
                return mainManager.sendListOfCommands(message);
            }
            case SENDING_TIME, SENDING_DESCRIPTION, SENDING_TITLE -> {
                return notificationManager.answerMessage(message, bot);
            }
            case SENDING_PRODUCT_GRAM, SENDING_PRODUCT_NAME -> {
                //TODO Логика добавления продукта, просмотра съеденных калорий за период
                return productManager.answerMessage(message, bot);
            }
        }
        return operationIsNotSupported(message, bot);
    }
}
