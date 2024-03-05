package telegram.Calories_Bot.service.contract;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.Calories_Bot.bot.Bot;

public abstract class AbstractManager {

    public abstract BotApiMethod<?> mainMenu(Message message, Bot bot);
    public abstract BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot);

}
