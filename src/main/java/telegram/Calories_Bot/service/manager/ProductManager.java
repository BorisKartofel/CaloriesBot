package telegram.Calories_Bot.service.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
            case SENDING_PRODUCT -> {
                return editProduct(message, user, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> editProduct(Message message, User user, Bot bot) {

        if (message.getText().length() < 3) return replyThatProductTextIsTooShort(message);
        List<Product> products = productRepo.findFirst16ByNameContainingIgnoreCase(message.getText());

        if (products.size() == 1) return saveProductAsEaten(user, message, products.get(0));
        return sendMatchingProducts(message, products);
    }

    private BotApiMethod<?> saveProductAsEaten(User user, Message message, Product product) {

        userProductRepo.save(UserProduct.builder()
                .userId(user.getId())
                .productId(product.getId())
                .status(Status.FINISHED)
                .build());

        user.setAction(Action.NONE);
        userRepo.save(user);

        return SendMessage.builder()
                .text("Съедено калорий - " + product.getKcal() + " ⚡️" +
                        "\n Белки - " + product.getProtein() +
                        "\n Жиры - " + product.getFat() +
                        "\n Углеводы - " + product.getCarbohydrate())
                .chatId(message.getChatId())
                .build();
    }

    private BotApiMethod<?> replyThatProductTextIsTooShort(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Текст слишком короткий для того, чтобы я мог найти подходящие продукты")
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
                    // PRODUCT_RETURN_{some uuid}
                else if (callbackDataWords[1].equals("RETURN"))
                    return displayReturnElement(query, UUID.fromString(callbackDataWords[2]));
            }
            case 4 -> {
                if (callbackDataWords[1].equals("SENDING") && callbackDataWords[2].equals("EATEN")) {
                    // PRODUCT_SENDING_EATEN_{some id}
                    return askProduct(query, callbackDataWords[3]);
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
