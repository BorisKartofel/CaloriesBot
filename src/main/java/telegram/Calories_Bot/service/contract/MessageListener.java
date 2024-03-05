package telegram.Calories_Bot.service.contract;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;

public interface MessageListener {
    BotApiMethod<?> answerMessage(Message message, Bot bot) throws TelegramApiException;
}
