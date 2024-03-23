package telegram.Calories_Bot.service.manager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.enums.Commands;
import telegram.Calories_Bot.service.contract.AbstractManager;
import telegram.Calories_Bot.service.contract.CommandListener;
import telegram.Calories_Bot.service.contract.QueryListener;
import telegram.Calories_Bot.service.factory.KeyboardFactory;

import java.util.List;

import static telegram.Calories_Bot.data.CallbackData.PRODUCT_MAIN;
import static telegram.Calories_Bot.data.CallbackData.notification_main;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MainManager extends AbstractManager implements CommandListener, QueryListener {

    KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Выберите действие")
                .replyMarkup(keyboardFactory.createInlineKeyboard(
                        List.of("Калории", "Уведомления"),
                        List.of(2),
                        List.of(PRODUCT_MAIN.name(), notification_main.name())
                )).build();
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Выберите действие")
                .replyMarkup(keyboardFactory.createInlineKeyboard(
                        List.of("Продукт", "Уведомление"),
                        List.of(1, 1),
                        List.of(PRODUCT_MAIN.name(), notification_main.name())
                )).build();
    }

    @Override
    public BotApiMethod<?> answerStartCommand(Message message, Bot bot) {
        return mainMenu(message, bot);
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        return mainMenu(query, bot);
    }

    /**
     * Sends a list of available {@link Commands} to user
     */
    public BotApiMethod<?> sendListOfCommands(Message message, Bot bot) {

        StringBuilder messageToSend = new StringBuilder("Доступные команды:\n");
        List<String> commands = Commands.getListOfBotCommands();
        for (String command : commands) {
            messageToSend.append(command);
            messageToSend.append('\n');
        }

        return SendMessage.builder().chatId(message.getChatId()).text(messageToSend.toString()).build();
    }

}
