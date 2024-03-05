package telegram.Calories_Bot.service.contract;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;

public abstract class AbstractHandler {

    public abstract BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException;

}
