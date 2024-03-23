package telegram.Calories_Bot.service.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.entity.UserProduct;
import telegram.Calories_Bot.entity.enums.Action;
import telegram.Calories_Bot.entity.enums.Status;
import telegram.Calories_Bot.repository.UserProductRepo;
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.contract.AbstractManager;
import telegram.Calories_Bot.service.contract.QueryListener;
import telegram.Calories_Bot.service.factory.KeyboardFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static telegram.Calories_Bot.data.CallbackData.*;

@Service
@Slf4j
public class ProductManager extends AbstractManager implements QueryListener {

    private final UserRepo userRepo;
    private final UserProductRepo userProductRepo;
    private final KeyboardFactory keyboardFactory;


    @Autowired
    public ProductManager(UserRepo userRepo, UserProductRepo userProductRepo, KeyboardFactory keyboardFactory) {
        this.userRepo = userRepo;
        this.userProductRepo = userProductRepo;
        this.keyboardFactory = keyboardFactory;
    }


    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return null;
    }

    public BotApiMethod<?> answerMessage(Message message, Bot bot) {

        try {
            bot.execute(
                    DeleteMessage.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId() - 1)
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }

        User user = userRepo.findByChatId(message.getChatId());

        switch (user.getAction()) {
            case SENDING_PRODUCT -> {
                // TODO Добавить логику
                return null;
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        switch (words.length) {
            case 2 -> {
                switch (words[1]) {
                    case "MAIN" -> {
                        // PRODUCT_MAIN
                        return mainProduct(query, bot);
                    }
                    case "ADD" -> {
                        // PRODUCT_ADD
                        return startAddingProduct(query, bot);
                    }
                }
            }
            case 3 -> {
                // PRODUCT_CREATE_NEW
                if (words[2].equals("NEW")) return null;
                // PRODUCT_RETURN_{some uuid}
                else if ("RETURN".equals(words[1])) return displayReturnElement(query, UUID.fromString(words[2]));
            }
            case 4 -> {
                if (words[1].equals("SENDING") && words[2].equals("EATEN")) {
                    // PRODUCT_SENDING_EATEN_{some id}
                    return askProduct(query, words[3]);
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> mainProduct(CallbackQuery query, Bot bot) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Выберите действие")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Добавить съеденый продукт"),
                                List.of(1),
                                List.of(PRODUCT_ADD.name())
                        )
                )
                .build();
    }

    private BotApiMethod<?> startAddingProduct(CallbackQuery query, Bot bot) {

        User user = userRepo.findByChatId(query.getMessage().getChatId());

        UUID uuid = userProductRepo.save(
                UserProduct.builder()
                        .userId(user.getId())
                        .status(Status.BUILDING)
                        .build()
        ).getId();

        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Добавьте продукт")
                .replyMarkup(chooseProductReplyMarkup(uuid))
                .build();
    }

    private InlineKeyboardMarkup chooseProductReplyMarkup(UUID uuid) {

        List<String> text = new ArrayList<>(3);
        UserProduct userProduct = userProductRepo.findById(uuid).get();

        if (userProduct.getProductId() != null) {
            text.add("✅ Продукт");
        } else {
            text.add("❌ Продукт");
        }
        text.add("\uD83D\uDD19 Главная");
        text.add("\uD83D\uDD50 Готово");

        return keyboardFactory.createInlineKeyboard(
                text,
                List.of(1, 2),
                List.of(
                        PRODUCT_SENDING_EATEN_.name() + uuid,
                        main.name(), PRODUCT_HAS_BEEN_EATEN.name()
                )
        );
    }

    private BotApiMethod<?> askProduct(CallbackQuery query, String uuid) {

        User user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_PRODUCT);
        user.setCurrentProductUUID(UUID.fromString(uuid));
        userRepo.save(user);

        return EditMessageText.builder()
                .text("⚡️ Напишите текстом, что Вы съели")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("\uD83D\uDD19 Назад"),
                                List.of(1),
                                List.of(PRODUCT_RETURN_ + uuid)
                        )
                )
                .build();
    }

    private BotApiMethod<?> displayReturnElement(CallbackQuery query, UUID uuid) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Добавьте продукт")
                .replyMarkup(chooseProductReplyMarkup(uuid))
                .build();
    }

}
