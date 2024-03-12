package telegram.Calories_Bot.service.contract;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
                .text("Я пока не умею обрабатывать такие запросы")
                .build();
    }

    protected BotApiMethod<?> operationIsNotSupported(CallbackQuery callbackQuery, Bot bot) {
        return SendMessage.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .text("Я пока не умею обрабатывать такие запросы")
                .build();
    }

}
