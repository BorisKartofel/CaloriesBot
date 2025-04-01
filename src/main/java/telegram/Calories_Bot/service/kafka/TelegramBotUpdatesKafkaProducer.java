package telegram.Calories_Bot.service.kafka;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class TelegramBotUpdatesKafkaProducer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramBotUpdatesKafkaProducer.class);
    private final KafkaTemplate<String, Update> kafkaTemplate;


    @Autowired
    public TelegramBotUpdatesKafkaProducer(KafkaTemplate<String, Update> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void produce(Update update) {
        log.info("Sending an Update to Kafka: {}", update.getUpdateId());
        kafkaTemplate.send("TelegramUpdatesTopic", update);

    }
}
