package telegram.Calories_Bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CaloriesBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaloriesBotApplication.class, args);

//        HTTPRequestSender requestSender = new HTTPRequestSender();
//        TelegramProperties telegramProperties = applicationContext.getBean(TelegramProperties.class);
//
//        requestSender.setTelegramBotWebHook(telegramProperties.getUrl(), telegramProperties.getToken());
    }
}
