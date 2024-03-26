package telegram.Calories_Bot.service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.service.contract.AbstractHandler;
import telegram.Calories_Bot.service.manager.MainManager;
import telegram.Calories_Bot.service.manager.NotificationManager;
import telegram.Calories_Bot.service.manager.ProductManager;

@Service
public class CallbackQueryHandler extends AbstractHandler {

    private final ProductManager productManager;
    private final NotificationManager notificationManager;
    private final MainManager mainManager;

    public CallbackQueryHandler(ProductManager productManager, NotificationManager notificationManager, MainManager mainManager) {
        this.productManager = productManager;
        this.notificationManager = notificationManager;
        this.mainManager = mainManager;
    }


    @Override
    public BotApiMethod<?> answer(BotApiObject object, Bot bot) throws TelegramApiException {
        var query = (CallbackQuery) object;
        String[] words = query.getData().split("_");
        switch (words[0]) {
            case "main" -> {
                return mainManager.answerQuery(query, words, bot);
            }
            case "notification" -> {
                return notificationManager.answerQuery(query, words, bot);
            }
            case "PRODUCT" -> {
                return productManager.answerQuery(query, words, bot);
            }
        }
        return operationIsNotSupported(query, bot);
    }

}
