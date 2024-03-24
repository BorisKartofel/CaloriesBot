package telegram.Calories_Bot.service.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.Product;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.entity.UserProduct;
import telegram.Calories_Bot.entity.enums.Action;
import telegram.Calories_Bot.entity.enums.Status;
import telegram.Calories_Bot.repository.ProductRepo;
import telegram.Calories_Bot.repository.UserProductRepo;
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.contract.AbstractManager;
import telegram.Calories_Bot.service.contract.MessageListener;
import telegram.Calories_Bot.service.contract.QueryListener;
import telegram.Calories_Bot.service.factory.KeyboardFactory;

import java.util.*;
import java.util.stream.Collectors;

import static telegram.Calories_Bot.data.CallbackData.*;

@Service
@Slf4j
public class ProductManager extends AbstractManager implements QueryListener, MessageListener {

    private final UserRepo userRepo;
    private final UserProductRepo userProductRepo;
    private final ProductRepo productRepo;
    private final KeyboardFactory keyboardFactory;


    @Autowired
    public ProductManager(UserRepo userRepo, UserProductRepo userProductRepo, ProductRepo productRepo, KeyboardFactory keyboardFactory) {
        this.userRepo = userRepo;
        this.userProductRepo = userProductRepo;
        this.productRepo = productRepo;
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

    @Override
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
            case SENDING_PRODUCT_NAME -> {
                return editProduct(message, user, bot);
            }
            case SENDING_PRODUCT_GRAM -> {
                return editGrams(message, user, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> editGrams(Message message, User user, Bot bot) {

        try {
            int grams = Integer.parseInt(message.getText());

            if (grams == 0) return replyThatProductGramsAreIncorrect(message);

            UserProduct userProduct = userProductRepo.findById(user.getCurrentProductUUID()).orElseThrow();

            userProduct.setProductGrams(grams);
            userProduct.setUserId(user.getId());
            userProduct.setStatus(Status.BUILDING);
            userProductRepo.save(userProduct);

            user.setAction(Action.NONE);
            userRepo.save(user);

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Настройте продукт")
                    .replyMarkup(editProductReplyMarkup(userProduct.getId()))
                    .build();

        } catch (NumberFormatException e) {
            return replyThatProductGramsAreIncorrect(message);
        }


    }

    private BotApiMethod<?> editProduct(Message message, User user, Bot bot) {

        if (message.getText().length() < 3) return replyThatProductTextIsTooShort(message);

        List<Product> products = productRepo.findAllByNameContainingIgnoreCase(message.getText());
        UserProduct userProduct = userProductRepo.findById(user.getCurrentProductUUID()).orElseThrow();

        for (Product product : products) {
            // Finds first exact occurrence with message and product's name
            if (product.getName().equals(message.getText())) {

                userProduct.setProductId(product.getId());
                userProduct.setUserId(user.getId());
                userProduct.setStatus(Status.BUILDING);
                userProductRepo.save(userProduct);

                user.setAction(Action.NONE);
                userRepo.save(user);

                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Настройте продукт")
                        .replyMarkup(editProductReplyMarkup(userProduct.getId()))
                        .build();
            }
        }

        return sendMatchingProducts(message, products);
    }

    private BotApiMethod<?> replyThatProductTextIsTooShort(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Текст слишком короткий для того, чтобы я мог найти подходящие продукты")
                .build();
    }

    private BotApiMethod<?> replyThatProductGramsAreIncorrect(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Количество грамм написано некорректно. Напишите число (больше нуля)")
                .build();
    }

    private BotApiMethod<?> sendMatchingProducts(Message message, List<Product> products) {
        if (products.isEmpty()) {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("По Вашему запросу не найдено ни одного продукта." +
                            "\nПопробуйте ввести название по-другому, либо создайте свой продукт, чтобы я его знал." +
                            "\n(Разработчик торопился и пока что создать кастомный продукт нельзя)")
                    // TODO Сделать так, чтоб было можно
                    .build();
        }

        List<Integer> configurationListForReplyKeyboard = new LinkedList<>();
        for (int i = 0; i < products.size(); i++) {
            configurationListForReplyKeyboard.add(1);
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format("По Вашему запросу найдено %d продуктов", products.size()))
                .replyMarkup(
                        keyboardFactory.createReplyKeyboard(
                                products.stream().map(Product::getName).collect(Collectors.toList()),
                                configurationListForReplyKeyboard
                        )
                )
                .build();
    }


    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] callbackDataWords, Bot bot) {
        switch (callbackDataWords.length) {
            case 2 -> {
                switch (callbackDataWords[1]) {
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
                // TODO Логика добавления кастомного продукта
                if (callbackDataWords[2].equals("NEW")) return null;

                    // PRODUCT_RETURN_{uuid}
                else if (callbackDataWords[1].equals("RETURN"))
                    return displayReturnElement(query, UUID.fromString(callbackDataWords[2]));
            }
            case 4 -> {
                switch (callbackDataWords[2]) {
                    case "EATEN" -> {
                        // PRODUCT_SENDING_EATEN_{uuid}
                        return askProduct(query, callbackDataWords[3]);
                    }
                    case "GRAM" -> {
                        return askGrams(query, callbackDataWords[3]);
                    }
                }
            }
            case 5 -> {
                // PRODUCT_HAS_BEEN_EATEN_{uuid}
                if (callbackDataWords[3].equals("EATEN")) return saveEatenProduct(query, callbackDataWords[4], bot);
            }
        }
        return null;
    }

        private BotApiMethod<?> saveEatenProduct(CallbackQuery query, String uuid, Bot bot) {

        Optional<UserProduct> userProduct = userProductRepo.findById(UUID.fromString(uuid));
        if (userProduct.isEmpty() || userProduct.get().getProductId() == null || userProduct.get().getProductGrams() == null) {
            return AnswerCallbackQuery.builder()
                    .callbackQueryId(query.getId())
                    .text("Заполните обязательные поля: Продукт и Граммы \uD83D\uDCA9")
                    .build();
        }

        Optional<Product> product = productRepo.findById(userProduct.get().getProductId());

        userProduct.get().setStatus(Status.FINISHED);
        userProductRepo.save(userProduct.get());

        try {
            bot.execute(
                    SendMessage.builder()
                            .text("Съедено калорий - " + product.get().getKcal() + " ⚡️" +
                                    "\n Белки - " + product.get().getProtein() +
                                    "\n Жиры - " + product.get().getFat() +
                                    "\n Углеводы - " + product.get().getCarbohydrate())
                            .chatId(query.getMessage().getChatId())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }

        return EditMessageText.builder()
                .text("✅ Успешно")
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("На главную"),
                                List.of(1),
                                List.of(main.name())
                        )
                )
                .build();
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
                .replyMarkup(editProductReplyMarkup(uuid))
                .build();
    }

    private InlineKeyboardMarkup editProductReplyMarkup(UUID uuid) {

        List<String> text = new ArrayList<>(3);
        UserProduct userProduct = userProductRepo.findById(uuid).get();

        if (userProduct.getProductId() != null) {
            text.add("\uD83D\uDFE2 Продукт");
        } else {
            text.add("\uD83D\uDD34 Продукт");
        }
        if (userProduct.getProductGrams() != null) {
            text.add("\uD83D\uDFE2 Граммы");
        } else {
            text.add("\uD83D\uDD34 Граммы");
        }
        text.add("\uD83D\uDD19 Главная");
        text.add("✅ Готово");

        return keyboardFactory.createInlineKeyboard(
                text,
                List.of(2, 2),
                List.of(
                        PRODUCT_SENDING_EATEN_.name() + uuid, PRODUCT_SENDING_GRAM_.name() + uuid,
                        main.name(), PRODUCT_HAS_BEEN_EATEN_.name() + uuid
                )
        );
    }

    private BotApiMethod<?> askProduct(CallbackQuery query, String uuid) {

        User user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_PRODUCT_NAME);
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

    private BotApiMethod<?> askGrams(CallbackQuery query, String uuid) {
        User user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_PRODUCT_GRAM);
        user.setCurrentProductUUID(UUID.fromString(uuid));
        userRepo.save(user);

        return EditMessageText.builder()
                .text("⚡️ Напишите текстом количество грамм")
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

        User user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.NONE);
        userRepo.save(user);

        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Добавьте продукт")
                .replyMarkup(editProductReplyMarkup(uuid))
                .build();
    }

}
