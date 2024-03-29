package telegram.Calories_Bot.service.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import telegram.Calories_Bot.entity.Notification;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.entity.enums.Action;
import telegram.Calories_Bot.entity.enums.Status;
import telegram.Calories_Bot.repository.NotificationRepo;
import telegram.Calories_Bot.repository.UserRepo;
import telegram.Calories_Bot.service.contract.AbstractManager;
import telegram.Calories_Bot.service.contract.MessageListener;
import telegram.Calories_Bot.service.contract.QueryListener;
import telegram.Calories_Bot.service.factory.KeyboardFactory;
import telegram.Calories_Bot.service.manager.notification.NotificationRunnableTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static telegram.Calories_Bot.data.CallbackData.*;


@Service
public class NotificationManager extends AbstractManager implements QueryListener, MessageListener {
    public final Logger log = LogManager.getLogger();
    private final KeyboardFactory keyboardFactory;
    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;

    public NotificationManager(KeyboardFactory keyboardFactory, NotificationRepo notificationRepo, UserRepo userRepo) {
        this.keyboardFactory = keyboardFactory;
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    @Override
    public BotApiMethod<?> mainMenu(Message message, Bot bot) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Настройте таймер")
                .replyMarkup(
                        editNotificationReplyMarkup(
                                String.valueOf(userRepo.findByChatId(message.getChatId()).getCurrentNotification())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> mainMenu(CallbackQuery query, Bot bot) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Выберите вид уведомления")
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("Таймер"),
                                List.of(1),
                                List.of(notification_new.name())
                        )
                )
                .build();
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {

        User user = userRepo.findByChatId(message.getChatId());

        try {
            bot.execute(
                    DeleteMessage.builder()
                            .chatId(message.getChatId())
                            .messageId(message.getMessageId() - 1)
                            .build()
            );
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }

        switch (user.getAction()) {
            case SENDING_TIME -> {
                return editTime(message, user, bot);
            }
            case SENDING_DESCRIPTION -> {
                return editDescription(message, user, bot);
            }
            case SENDING_TITLE -> {
                return editTitle(message, user, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> editTitle(Message message, User user, Bot bot) {
        var notification = notificationRepo.findById(user.getCurrentNotification()).orElseThrow();
        notification.setTitle(message.getText());
        notificationRepo.save(notification);

        user.setAction(Action.NONE);
        userRepo.save(user);
        return mainMenu(message, bot);
    }

    private BotApiMethod<?> editDescription(Message message, User user, Bot bot) {
        var notification = notificationRepo.findById(user.getCurrentNotification()).orElseThrow();
        notification.setDescription(message.getText());
        notificationRepo.save(notification);

        user.setAction(Action.NONE);
        userRepo.save(user);
        return mainMenu(message, bot);
    }

    private BotApiMethod<?> editTime(Message message, User user, Bot bot) {
        var notification = notificationRepo.findById(user.getCurrentNotification()).orElseThrow();

        var messageText = message.getText().strip();
        var pattern = Pattern.compile("^[0-9]{2}:[0-5][0-9]:[0-5][0-9]$").matcher(messageText);
        if (pattern.matches()) {
            var nums = messageText.split(":");
            int seconds = Integer.parseInt(nums[0]) * 3600 + Integer.parseInt(nums[1]) * 60 + Integer.parseInt(nums[2]);
            notification.setSeconds(seconds);
        } else {
            return SendMessage.builder()
                    .text("Некорректный формат времени." +
                            "\nНеобходимый формат - ЧЧ:ММ:СС (01:00:30 - один час, ноль минут, тридцать секунд)")
                    .chatId(message.getChatId())
                    .replyMarkup(
                            keyboardFactory.createInlineKeyboard(
                                    List.of("\uD83D\uDD19 Назад"),
                                    List.of(1),
                                    List.of(notification_back_ + String.valueOf(user.getCurrentNotification()))
                            )
                    )
                    .build();
        }
        notificationRepo.save(notification);
        user.setAction(Action.NONE);
        userRepo.save(user);
        return mainMenu(message, bot);
    }

    @Override
    public BotApiMethod<?> answerQuery(CallbackQuery query, String[] words, Bot bot) {
        switch (words.length) {
            case 2 -> {
                switch (words[1]) {
                    case "main" -> {
                        return mainMenu(query, bot);
                    }
                    case "new" -> {
                        return newNotification(query);
                    }
                }
            }
            case 3 -> {
                switch (words[1]) {
                    case "back" -> {
                        return displayEditNotificationElement(query, words[2]);
                    }
                    case "done" -> {
                        return sendNotification(query, words[2], bot);
                    }
                }
            }
            case 4 -> {
                switch (words[1]) {
                    case "edit" -> {
                        switch (words[2]) {
                            case "title" -> {
                                return askTitle(query, words[3]);
                            }
                            case "descr" -> {
                                return askDescription(query, words[3]);
                            }
                            case "time" -> {
                                return askSeconds(query, words[3]);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> sendNotification(CallbackQuery query, String id, Bot bot) {

        Optional<Notification> notification = notificationRepo.findById(UUID.fromString(id));

        if (notification.isEmpty()) return AnswerCallbackQuery.builder()
                .callbackQueryId(query.getId())
                .text("Уведомления не существует")
                .build();

        if (notification.get().getTitle() == null || notification.get().getTitle().isBlank() || notification.get().getSeconds() == null) {
            return AnswerCallbackQuery.builder()
                    .callbackQueryId(query.getId())
                    .text("Заполните обязательные поля: Заголовок и Время")
                    .build();
        }

        try {
            bot.execute(
                    AnswerCallbackQuery.builder()
                            .text("Таймер сработает через " + notification.get().getSeconds() + " секунд \uD83D\uDCA9")
                            .callbackQueryId(query.getId())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }

        notification.get().setStatus(Status.WAITING);
        notificationRepo.save(notification.get());
        Thread.startVirtualThread(
                new NotificationRunnableTask(
                        bot,
                        query.getMessage().getChatId(),
                        notification.get(),
                        notificationRepo
                )
        );

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

    private BotApiMethod<?> displayEditNotificationElement(CallbackQuery query, String id) {
        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Настройте таймер")
                .replyMarkup(editNotificationReplyMarkup(id))
                .build();
    }

    private BotApiMethod<?> askSeconds(CallbackQuery query, String id) {
        var user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_TIME);
        user.setCurrentNotification(UUID.fromString(id));
        userRepo.save(user);
        return EditMessageText.builder()
                .text("""
                        ⚡️ Введите время, через которое сработает таймер.
                        Формат - ЧЧ:ММ:СС
                        Например - (01:00:30 - один час, ноль минут, тридцать секунд)
                        """)
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("\uD83D\uDD19 Назад"),
                                List.of(1),
                                List.of(notification_back_ + id)
                        )
                )
                .build();
    }

    private BotApiMethod<?> askDescription(CallbackQuery query, String id) {
        var user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_DESCRIPTION);
        user.setCurrentNotification(UUID.fromString(id));
        userRepo.save(user);
        return EditMessageText.builder()
                .text("⚡️Напишите в чат описание для таймера (не обязательно).")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("\uD83D\uDD19 Назад"),
                                List.of(1),
                                List.of(notification_back_ + id)
                        )
                )
                .build();
    }

    private BotApiMethod<?> askTitle(CallbackQuery query, String id) {
        User user = userRepo.findByChatId(query.getMessage().getChatId());
        user.setAction(Action.SENDING_TITLE);
        user.setCurrentNotification(UUID.fromString(id));
        userRepo.save(user);
        return EditMessageText.builder()
                .text("⚡️ Напишите в следующем сообщении краткий заголовок таймера")
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId())
                .replyMarkup(
                        keyboardFactory.createInlineKeyboard(
                                List.of("\uD83D\uDD19 Назад"),
                                List.of(1),
                                List.of(notification_back_ + id)
                        )
                )
                .build();
    }

    private BotApiMethod<?> newNotification(CallbackQuery query) {

        User user = userRepo.findByChatId(query.getMessage().getChatId());

        String uuid = String.valueOf(notificationRepo.save(
                Notification.builder()
                        .user(user)
                        .status(Status.BUILDING)
                        .build()
        ).getId());

        return EditMessageText.builder()
                .chatId(query.getMessage().getChatId())
                .messageId(query.getMessage().getMessageId())
                .text("Настройте таймер")
                .replyMarkup(editNotificationReplyMarkup(uuid))
                .build();

    }

    private InlineKeyboardMarkup editNotificationReplyMarkup(String uuid) {

        List<String> text = new ArrayList<>(3);
        Optional<Notification> notification = notificationRepo.findById(UUID.fromString(uuid));

        if (notification.isEmpty()) return thisActionIsNotAvailableReplyMarkup();

        if (notification.get().getTitle() != null && !notification.get().getTitle().isBlank()) {
            text.add("\uD83D\uDFE2 Заголовок");
        } else {
            text.add("\uD83D\uDFE1 Заголовок");
        }
        if (notification.get().getSeconds() != null && notification.get().getSeconds() != 0) {
            text.add("\uD83D\uDFE2 Время");
        } else {
            text.add("\uD83D\uDFE1 Время");
        }
        if (notification.get().getDescription() != null && !notification.get().getDescription().isBlank()) {
            text.add("\uD83D\uDFE2 Описание");
        } else {
            text.add("\uD83D\uDFE1 Описание");
        }
        text.add("\uD83D\uDD19 Главная");
        text.add("✅ Готово");
        return keyboardFactory.createInlineKeyboard(
                text,
                List.of(2, 1, 2),
                List.of(
                        notification_edit_title_.name() + uuid, notification_edit_time_.name() + uuid,
                        notification_edit_descr_.name() + uuid,
                        main.name(), notification_done_.name() + uuid
                )
        );
    }

    private InlineKeyboardMarkup thisActionIsNotAvailableReplyMarkup() {
        return keyboardFactory.createInlineKeyboard(
                List.of("Это действие недоступно. Нажмите, чтобы вернуться на Главную"),
                List.of(1),
                List.of(main.name())
        );
    }
}
