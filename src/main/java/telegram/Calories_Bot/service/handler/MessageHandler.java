package telegram.Calories_Bot.service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import telegram.Calories_Bot.service.manager.notification.NotificationManager;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageHandler extends AbstractHandler {
    UserRepo userRepo;
    NotificationManager notificationManager;
    MainManager mainManager;

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException {
        Message message = (Message) object;
        User user = userRepo.findByChatId(message.getChatId());
        switch (user.getAction()) {
            case NONE -> {
                return mainManager.sendListOfCommands(message, bot);
            }
            case SENDING_TIME, SENDING_DESCRIPTION, SENDING_TITLE -> {
                return notificationManager.answerMessage(message, bot);
            }
        }
        return operationIsNotSupported(message, bot);
    }
}
