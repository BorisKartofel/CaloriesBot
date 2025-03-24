package telegram.Calories_Bot.service.manager.notification;

import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.entity.Notification;
import telegram.Calories_Bot.entity.enums.Status;
import telegram.Calories_Bot.repository.NotificationRepo;


public class NotificationRunnableTask implements Runnable {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(NotificationRunnableTask.class);
    protected final Bot bot;
    protected final Long chatId;
    protected final Notification notification;
    protected final NotificationRepo notificationRepo;


    public NotificationRunnableTask(
            Bot bot,
            Long chatId,
            Notification notification,
            NotificationRepo notificationRepo
    ) {
        this.bot = bot;
        this.chatId = chatId;
        this.notification = notification;
        this.notificationRepo = notificationRepo;
    }


    @Override
    public void run() {
        try {
            Thread.sleep(notification.getSeconds() * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        try {
            bot.execute(
                    sendNotification()
            );
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        notification.setStatus(Status.FINISHED);
        notificationRepo.save(notification);
    }

    private BotApiMethod<?> sendNotification() {
        return SendMessage.builder()
                .chatId(chatId)
                .text(
                        "⚡️ Напоминание: " + notification.getTitle() + "\n"
                        + "❗️ " + (notification.getDescription() == null ? "Нет описания" : notification.getDescription() + "\n"
                ))
                .build();

    }

}
