package telegram.Calories_Bot.service.contract;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;

import java.util.List;

import static telegram.Calories_Bot.data.CallbackData.notification_main;

public abstract class AbstractHandler {

    public abstract BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException;

    protected BotApiMethod<?> operationIsNotSupported(Message message, Bot bot) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Я не знаю такой команды")
                .build();
    }

}
