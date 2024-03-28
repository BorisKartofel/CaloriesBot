package telegram.Calories_Bot.service.contract;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;

public abstract class AbstractManager {

    public final Logger log = LogManager.getLogger();


    public abstract BotApiMethod<?> mainMenu(Message message, Bot bot);

    public abstract BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot);

    protected void deleteMessage(Message message, Bot bot){
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
    }

}
