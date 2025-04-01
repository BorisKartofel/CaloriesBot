package telegram.Calories_Bot.service.kafka;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.Calories_Bot.bot.Bot;
import telegram.Calories_Bot.service.UpdateDispatcher;


@Service
public class TelegramBotUpdatesKafkaConsumer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramBotUpdatesKafkaConsumer.class);
    private final UpdateDispatcher updateDispatcher;
    private final Bot bot;

    @Autowired
    public TelegramBotUpdatesKafkaConsumer(UpdateDispatcher updateDispatcher, Bot bot) {
        this.updateDispatcher = updateDispatcher;
        this.bot = bot;
    }

    @KafkaListener(
            topics = "TelegramUpdatesTopic",
            groupId = "telegram-bot-updates",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Update update) {
        log.info("Received an Update: {}", update.getUpdateId());
        try {
            bot.execute(updateDispatcher.distribute(update, bot));
        } catch (TelegramApiException e) {
            log.error("Error processing an Update {}", update.getUpdateId(), e);
            throw new RuntimeException(e); // Spring Kafka обработает это через DefaultErrorHandler
        }
    }


}
