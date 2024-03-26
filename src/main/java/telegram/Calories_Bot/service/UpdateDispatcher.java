package telegram.Calories_Bot.service;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.entity.enums.Action;
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.handler.CallbackQueryHandler;
import telegram.Calories_Bot.service.handler.CommandHandler;
import telegram.Calories_Bot.service.handler.MessageHandler;

import java.time.LocalDateTime;

@Service
public class UpdateDispatcher {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UpdateDispatcher.class);
    private final MessageHandler messageHandler;
    private final CommandHandler commandHandler;
    private final CallbackQueryHandler queryHandler;
    private final UserRepo userRepo;

    public UpdateDispatcher(MessageHandler messageHandler, CommandHandler commandHandler, CallbackQueryHandler queryHandler, UserRepo userRepo) {
        this.messageHandler = messageHandler;
        this.commandHandler = commandHandler;
        this.queryHandler = queryHandler;
        this.userRepo = userRepo;
    }

    public BotApiMethod<?> distribute(Update update, Bot bot) {
        try {
            if (update.hasCallbackQuery()) {
                checkUser(update.getCallbackQuery().getMessage().getChatId());
                return queryHandler.answer(update.getCallbackQuery(), bot);
            }
            if (update.hasMessage()) {
                Message message = update.getMessage();
                checkUser(message.getChatId());
                if (message.hasText()) {
                    if (message.getText().charAt(0) == '/') {
                        return commandHandler.answer(message, bot);
                    }
                    return messageHandler.answer(message, bot);
                }
            }
            log.warn("Unsupported update type: " + update);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void checkUser(Long chatId) {
        if (userRepo.existsByChatId(chatId)) {
            return;
        }
        userRepo.save(
                User.builder()
                        .action(Action.NONE)
                        .registeredAt(LocalDateTime.now())
                        .chatId(chatId)
                        .firstName("Unnamed User")
                        .build()
        );
    }

}
