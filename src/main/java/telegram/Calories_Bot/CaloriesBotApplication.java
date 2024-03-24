package telegram.Calories_Bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import telegram.Calories_Bot.entity.Product;
import telegram.Calories_Bot.entity.User;
import telegram.Calories_Bot.repository.UserRepo;

import java.util.List;

@SpringBootApplication
public class CaloriesBotApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CaloriesBotApplication.class, args);

//        HTTPRequestSender requestSender = new HTTPRequestSender();
//        TelegramProperties telegramProperties = applicationContext.getBean(TelegramProperties.class);
//
//        requestSender.setTelegramBotWebHook(telegramProperties.getUrl(), telegramProperties.getToken());
    }
}
