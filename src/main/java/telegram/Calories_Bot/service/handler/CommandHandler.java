package telegram.Calories_Bot.service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.service.contract.AbstractHandler;
import telegram.Calories_Bot.service.manager.MainManager;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandHandler extends AbstractHandler {

    MainManager mainManager;

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) {
        Message message = (Message) object;
        if ("/start".equals(message.getText())) {
            return mainManager.answerCommand(message, bot);
        }
        return operationIsNotSupported(message, bot);
    }
}