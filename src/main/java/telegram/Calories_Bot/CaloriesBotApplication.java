package telegram.Calories_Bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CaloriesBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaloriesBotApplication.class, args);
    }
    // TODO Посылать пост-запрос на телеграм API для регистрации бота
}
