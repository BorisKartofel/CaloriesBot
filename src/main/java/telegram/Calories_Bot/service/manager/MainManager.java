package telegram.Calories_Bot.service.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.entity.enums.Commands;
import telegram.Calories_Bot.entity.enums.Status;
import telegram.Calories_Bot.repository.NotificationRepo;
import telegram.Calories_Bot.repository.UserProductRepo;
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.contract.AbstractManager;
import telegram.Calories_Bot.service.contract.CommandListener;
import telegram.Calories_Bot.service.contract.QueryListener;
import telegram.Calories_Bot.service.factory.KeyboardFactory;

import java.util.List;

import static telegram.Calories_Bot.data.CallbackData.PRODUCT_MAIN;
import static telegram.Calories_Bot.data.CallbackData.notification_main;

@Service
public class MainManager extends AbstractManager implements CommandListener, QueryListener {

    private final KeyboardFactory keyboardFactory;
    private final UserRepo userRepo;
    private final UserProductRepo userProductRepo;
    private final NotificationRepo notificationRepo;


    @Autowired
    public MainManager(KeyboardFactory keyboardFactory, UserRepo userRepo, UserProductRepo userProductRepo, NotificationRepo notificationRepo) {
        this.keyboardFactory = keyboardFactory;
        this.userRepo = userRepo;
        this.userProductRepo = userProductRepo;
        this.notificationRepo = notificationRepo;
    }


    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Выберите действие")
                .replyMarkup(keyboardFactory.createInlineKeyboard(
                        List.of("Продукты", "Уведомления"),
                        List.of(2),
                        List.of(PRODUCT_MAIN.name(), notification_main.name())
                )).build();
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {

        cleanUpProductAndNotificationInBuildingProcessIfExist(query);

        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Выберите действие")
                .replyMarkup(keyboardFactory.createInlineKeyboard(
                        List.of("Продукты", "Уведомления"),
                        List.of(2),
                        List.of(PRODUCT_MAIN.name(), notification_main.name())
                )).build();
    }

    /**
     * Method needed in case of some user pressed 'Главная' button while building a Product
     **/
    private void cleanUpProductAndNotificationInBuildingProcessIfExist(CallbackQuery query) {
        User user = userRepo.findByChatId(query.getMessage().getChatId());
        userProductRepo.deleteUserProductByUserIdAndStatus(user.getId(), Status.BUILDING);
        notificationRepo.deleteNotificationByUserIdAndStatus(user.getId(), Status.BUILDING);
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
