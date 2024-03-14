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
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.contract.AbstractHandler;
import telegram.Calories_Bot.service.manager.notification.NotificationManager;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageHandler extends AbstractHandler {
    UserRepo userRepo;
    NotificationManager notificationManager;
    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException {
        var message = (Message) object;
        var user = userRepo.findByChatId(message.getChatId());
        switch (user.getAction()) {
            case NONE -> {
                return null;
            }
            case SENDING_TIME, SENDING_DESCRIPTION, SENDING_TITLE -> {
                return notificationManager.answerMessage(message, bot);
            }
        }
        return operationIsNotSupported(message, bot);
    }
}
