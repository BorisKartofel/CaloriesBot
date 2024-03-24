package telegram.Calories_Bot.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.service.contract.AbstractHandler;
import telegram.Calories_Bot.service.manager.MainManager;

@Service
@Slf4j
public class CommandHandler extends AbstractHandler {

    private final MainManager mainManager;

    @Autowired
    public CommandHandler(MainManager mainManager) {
        this.mainManager = mainManager;
    }

    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) {
        Message message = (Message) object;
        if ("/start".equals(message.getText())) {

            try {
                bot.execute(
                        DeleteMessage.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .build()
                );
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }

            return mainManager.answerStartCommand(message, bot);
        }

        //TODO Добавить обработку других команд
        return operationIsNotSupported(message, bot);
    }
}