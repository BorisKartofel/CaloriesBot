package telegram.Calories_Bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import telegram.Calories_Bot.config.TelegramProperties;
import telegram.Calories_Bot.util.HTTPRequestSender;

@SpringBootApplication
public class CaloriesBotApplication {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(CaloriesBotApplication.class, args);

//        HTTPRequestSender requestSender = new HTTPRequestSender();
//        TelegramProperties telegramProperties = applicationContext.getBean(TelegramProperties.class);
//
//        requestSender.setTelegramBotWebHook(telegramProperties.getUrl(), telegramProperties.getToken());
    }
}
