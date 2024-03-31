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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    public ProductManager(UserRepo userRepo, UserProductRepo userProductRepo, ProductRepo productRepo,
                          KeyboardFactory keyboardFactory) {
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
            case SENDING_PRODUCT_PERIOD -> {
                return sendInfoForAPeriod(message, user, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> sendInfoForAPeriod(Message message, User user, Bot bot) {

        try {
            int pastDays = Integer.parseInt(message.getText());
            if (pastDays < 0) return replyThatDataIsIncorrect(message, bot);

            LocalDateTime dateTimeNow = LocalDateTime.now();
            LocalDateTime pastTime = LocalDateTime.now().with(LocalTime.MIN).minusDays(pastDays);

            user.setAction(Action.NONE);
            userRepo.save(user);

            Product accumulativeProduct = new Product(0, null, 0f, 0f, 0f, 0, null);
            List<UserProduct> userProducts = userProductRepo.findUserProductsByEatingTimeIsGreaterThan(pastTime);
            HashMap<Integer, Integer> uniqueAccumulativeProductIdsAndGrams = new HashMap<>();

            for (UserProduct userProduct : userProducts) {
                if (uniqueAccumulativeProductIdsAndGrams.containsKey(userProduct.getProductId()))
                    uniqueAccumulativeProductIdsAndGrams.put(userProduct.getProductId(),
                            uniqueAccumulativeProductIdsAndGrams.get(userProduct.getProductId()) + userProduct.getProductGrams());
                else
                    uniqueAccumulativeProductIdsAndGrams.put(userProduct.getProductId(), userProduct.getProductGrams());
            }

            for (var uniqueProductsEntrySet : uniqueAccumulativeProductIdsAndGrams.entrySet()) {
                Optional<Product> product = productRepo.findById(uniqueProductsEntrySet.getKey());
                if (product.isEmpty()) return actionIsNoLongerAccessibleBecauseUuidIsIncorrect(message);

                accumulativeProduct.setKcal(
                        accumulativeProduct.getKcal() +
                                (product.get().getKcal() * uniqueProductsEntrySet.getValue() / 100)
                );
                accumulativeProduct.setProtein(
                        accumulativeProduct.getProtein() +
                                (product.get().getProtein() * uniqueProductsEntrySet.getValue() / 100)
                );
                accumulativeProduct.setFat(
                        accumulativeProduct.getFat() +
                                (product.get().getFat() * uniqueProductsEntrySet.getValue() / 100)
                );
                accumulativeProduct.setCarbohydrate(
                        accumulativeProduct.getCarbohydrate() +
                                (product.get().getCarbohydrate() * uniqueProductsEntrySet.getValue() / 100)
                );
            }

            deleteMessage(message, bot);

            var formatter = DateTimeFormatter.ofPattern("d MMMM (HH:mm)", new Locale("ru"));
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(String.format("Статистика за период с %s по %s:\nКалории - %d\nБелки - %.2f\nЖиры - %.2f\nУглеводы - %.2f",
                            pastTime.format(formatter),
                            dateTimeNow.format(formatter),
                            accumulativeProduct.getKcal(),
                            accumulativeProduct.getProtein(),
                            accumulativeProduct.getFat(),
                            accumulativeProduct.getCarbohydrate())
                    )
                    .build();

        } catch (NumberFormatException e) {
            log.debug(e.getMessage());
            replyThatDataIsIncorrect(message, bot);
        } catch (NullPointerException | IllegalArgumentException e) {
            log.debug(e.getMessage());
            return actionIsNoLongerAccessibleBecauseUuidIsIncorrect(message);
        }

        return null;
    }

/*
    private String amountOfDaysInCorrectDaysDeclination(int pastDays) {
        if (pastDays % 10 == 1 && pastDays != 11) return pastDays + " день:";
        else if (pastDays % 10 < 5 && pastDays % 100 != 12 && pastDays % 100 != 13 && pastDays % 100 != 14)
            return pastDays + " дня:";
        else return pastDays + "дней:";
    }
*/

    private BotApiMethod<?> editGrams(Message message, User user, Bot bot) {

        try {
            int grams = Integer.parseInt(message.getText());

            if (grams < 1) return replyThatDataIsIncorrect(message, bot);

            Optional<UserProduct> userProduct = userProductRepo.findById(user.getCurrentProductUUID());
            if (userProduct.isEmpty()) return actionIsNoLongerAccessibleBecauseUuidIsIncorrect(message);

            userProduct.get().setProductGrams(grams);
            userProduct.get().setUserId(user.getId());
            userProduct.get().setStatus(Status.BUILDING);
            userProductRepo.save(userProduct.get());

            user.setAction(Action.NONE);
            userRepo.save(user);

            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("Настройте продукт")
                    .replyMarkup(replyMarkupForEditingProduct(userProduct.get().getId()))
                    .build();

        } catch (NumberFormatException e) {

            try {
                bot.execute(
                        DeleteMessage.builder()
                                .chatId(message.getChatId())
                                .messageId(message.getMessageId())
                                .build()
                );
            } catch (TelegramApiException err) {
                log.error(err.getMessage());
            }

            return replyThatDataIsIncorrect(message, bot);
        }


    }

    private BotApiMethod<?> editProduct(Message message, User user, Bot bot) {

        if (message.getText().length() < 3) return replyThatProductTextIsTooShort(message, bot);

        List<Product> products = productRepo.findAllByNameContainingIgnoreCase(message.getText());
        Optional<UserProduct> userProduct = userProductRepo.findById(user.getCurrentProductUUID());

        if (userProduct.isEmpty()) return actionIsNoLongerAccessibleBecauseUuidIsIncorrect(message);

        for (Product product : products) {
            // Finds first exact occurrence with message and product's name
            if (product.getName().equals(message.getText())) {

                userProduct.get().setProductId(product.getId());
                userProduct.get().setUserId(user.getId());
                userProduct.get().setStatus(Status.BUILDING);
                userProductRepo.save(userProduct.get());

                user.setAction(Action.NONE);
                userRepo.save(user);

                return SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Настройте продукт")
                        .replyMarkup(replyMarkupForEditingProduct(userProduct.get().getId()))
                        .build();
            }
        }

        return sendMatchingProducts(message, products, bot);
    }

    private BotApiMethod<?> actionIsNoLongerAccessibleBecauseUuidIsIncorrect(Message message) {
        return EditMessageText
                .builder()
                .text("Действие больше недоступно")
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .replyMarkup(keyboardFactory.createInlineKeyboard(
                        List.of("На Главную"),
                        List.of(1),
                        List.of(main.name())
                ))
                .build();
    }

    private BotApiMethod<?> replyThatProductTextIsTooShort(Message message, Bot bot) {

        deleteMessage(message, bot);

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Текст слишком короткий для того, чтобы я мог найти подходящие продукты. Попробуйте снова")
                .build();
    }

    private BotApiMethod<?> replyThatDataIsIncorrect(Message message, Bot bot) {

        deleteMessage(message, bot);

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Некорректные данные. Попробуйте еще раз")
                .build();
    }

    private BotApiMethod<?> sendMatchingProducts(Message message, List<Product> products, Bot bot) {

        deleteMessage(message, bot);

        if (products.isEmpty()) {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("""
                            По Вашему запросу не найдено ни одного продукта.
                            Попробуйте ввести название по-другому, либо создайте свой продукт, чтобы я его знал.""")
                    // TODO Сделать так, чтоб было можно создавать кастомный продукт
                    .build();
        }

        List<Integer> configurationListForReplyKeyboard = new LinkedList<>();
        for (int i = 0; i < products.size(); i++) {
            configurationListForReplyKeyboard.add(1);
        }

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(String.format("По Вашему запросу найдено продуктов: " + products.size()))
                .replyMarkup(
                        keyboardFactory.createReplyKeyboard(
                                products.stream().map(Product::getName).sorted(Comparator.comparingInt(String::length)).collect(Collectors.toList()),
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
                        return mainProduct(query);
                    }
                    case "ADD" -> {
                        // PRODUCT_ADD
                        return startAddingProduct(query);
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
                if (callbackDataWords[2].equals("BEEN") && callbackDataWords[3].equals("EATEN"))
                    return saveEatenProduct(query, callbackDataWords[4], bot);
                // PRODUCT_SHOW_EATEN_PAST_{amount of days}
                if (callbackDataWords[3].equals("PAST"))
                    return eatenCaloriesForSpecifiedPeriod(query, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> eatenCaloriesForSpecifiedPeriod(CallbackQuery query, Bot bot) {
        return askPeriod(query, bot);
    }

    private BotApiMethod<?> askPeriod(CallbackQuery query, Bot bot) {

        User user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_PRODUCT_PERIOD);
        userRepo.save(user);

        return EditMessageText.builder()
                .text("⚡️ Напишите числом, за сколько прошедших дней Вам выдать статистику")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("\uD83D\uDFE1 Отмена"),
                                List.of(1),
                                List.of(main.name())
                        )
                )
                .build();

    }

    private BotApiMethod<?> saveEatenProduct(CallbackQuery query, String uuid, Bot bot) {

        Optional<UserProduct> userProduct = userProductRepo.findById(UUID.fromString(uuid));
        if (userProduct.isEmpty() || userProduct.get().getProductId() == null || userProduct.get().getProductGrams() == null) {
            return replyThatProductAndGramsFieldsShouldNotBeEmpty(query);
        }

        Optional<Product> product = productRepo.findById(userProduct.get().getProductId());
        if (product.isEmpty()) {
            return replyThatProductWithSuchIdDoNotExists(query);
        }

        userProduct.get().setStatus(Status.FINISHED);
        userProductRepo.save(userProduct.get());

        String text = String.format("Съедено калорий - %d ⚡️\nБелки - %.2f\nЖиры - %.2f\nУглеводы - %.2f",
                product.get().getKcal() * userProduct.get().getProductGrams() / 100,
                product.get().getProtein() * userProduct.get().getProductGrams() / 100,
                product.get().getFat() * userProduct.get().getProductGrams() / 100,
                product.get().getCarbohydrate() * userProduct.get().getProductGrams() / 100);

        return EditMessageText.builder()
                .text(text)
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

    private BotApiMethod<?> mainProduct(CallbackQuery query) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Выберите действие")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Добавить съеденный продукт", "Съеденные калории за период"),
                                List.of(1, 1),
                                List.of(PRODUCT_ADD.name(), PRODUCT_SHOW_EATEN_PAST_DAYS.name())
                        )
                )
                .build();
    }

    private BotApiMethod<?> startAddingProduct(CallbackQuery query) {

        User user = userRepo.findByChatId(query.getMessage().getChatId());

        UUID uuid = userProductRepo.save(
                UserProduct.builder()
                        .userId(user.getId())
                        .status(Status.BUILDING)
                        .eatingTime(LocalDateTime.now())
                        .build()
        ).getId();

        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Добавьте продукт")
                .replyMarkup(replyMarkupForEditingProduct(uuid))
                .build();
    }

    private InlineKeyboardMarkup replyMarkupForEditingProduct(UUID uuid) {

        List<String> text = new ArrayList<>(3);
        Optional<UserProduct> userProduct = userProductRepo.findById(uuid);
        if (userProduct.isEmpty()) return null;

        if (userProduct.get().getProductId() != null) {
            text.add("\uD83D\uDFE2 Продукт");
        } else {
            text.add("\uD83D\uDFE1 Добавить продукт");
        }
        if (userProduct.get().getProductGrams() != null) {
            text.add("\uD83D\uDFE2 Граммы");
        } else {
            text.add("\uD83D\uDFE1 Добавить граммы");
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
                .replyMarkup(replyMarkupForEditingProduct(uuid))
                .build();
    }

    private BotApiMethod<?> replyThatProductAndGramsFieldsShouldNotBeEmpty(CallbackQuery query) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(query.getId())
                .text("Заполните обязательные поля: Продукт и Граммы \uD83D\uDCA9")
                .build();
    }

    private BotApiMethod<?> replyThatProductWithSuchIdDoNotExists(CallbackQuery query) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(query.getId())
                .text("Продукта с таким id не существует \uD83D\uDCA9")
                .build();
    }


}
