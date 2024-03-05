package telegram.Calories_Bot.bot;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.Calories_Bot.config.TelegramProperties;
import telegram.Calories_Bot.service.UpdateDispatcher;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Bot extends TelegramWebhookBot {

    TelegramProperties telegramProperties;
    UpdateDispatcher updateDispatcher;

    public Bot(TelegramProperties telegramProperties, UpdateDispatcher updateDispatcher) {
        super(telegramProperties.getToken());
        this.telegramProperties = telegramProperties;
        this.updateDispatcher = updateDispatcher;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateDispatcher.distribute(update, this);
    }

    @Override
    public String getBotPath() {
        return telegramProperties.getUrl();
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.getName();
    }
}