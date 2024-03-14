package telegram.Calories_Bot.entity.enums;

import java.util.LinkedList;
import java.util.List;

public enum Commands {
    START("/start"),
    PRODUCT("/product"),
    NOTIFICATION("/notify");

    private final String command;

    Commands(String botCommand) {
        this.command = botCommand;
    }
    
    public static List<String> getListOfBotCommands(){
        List<String> commands = new LinkedList<>();
        for (Commands command :
                Commands.values()) {
            commands.add(command.command);
        }
        return commands;
    }
}
