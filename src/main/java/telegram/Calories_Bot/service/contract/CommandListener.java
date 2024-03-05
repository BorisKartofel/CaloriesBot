package telegram.Calories_Bot.service.contract;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.Calories_Bot.bot.Bot;

public interface CommandListener {
    BotApiMethod<?> answerCommand(Message message, Bot bot);
}
