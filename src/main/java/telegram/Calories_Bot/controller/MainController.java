package telegram.Calories_Bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import telegram.Calories_Bot.service.kafka.TelegramBotUpdatesKafkaProducer;

@RestController
public class MainController {

    private final TelegramBotUpdatesKafkaProducer telegramBotUpdatesKafkaProducer;


    @Autowired
    public MainController(TelegramBotUpdatesKafkaProducer telegramBotUpdatesKafkaProducer) {
        this.telegramBotUpdatesKafkaProducer = telegramBotUpdatesKafkaProducer;
    }


    @PostMapping
    public HttpStatus listener(@RequestBody Update update) {
        telegramBotUpdatesKafkaProducer.produce(update);
        return HttpStatus.ACCEPTED;
    }

}
