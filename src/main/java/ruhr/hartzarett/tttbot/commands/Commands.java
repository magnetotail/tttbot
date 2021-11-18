package ruhr.hartzarett.tttbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Commands implements Command {


    REGISTER("register", "Registriert dich", List.of(CommandOptions.STEAMNAME_REQUIRED)),
    REMOVE("remove", "Entfernt dich", Collections.emptyList()),
    SHOW_FOR_STEAMNAME("showForSteam", "Zeigt an, welcher Member mit einem Steamnamen verknüpft ist", List.of(CommandOptions.STEAMNAME_REQUIRED)),
    SHOW("show", "Zeigt an, welcher Steamname mit dir verknüpft ist", Collections.emptyList()),
    LIST("list", "Listet alle registrierten Member mit zugehörigem Steamnamen auf", Collections.emptyList()),
    HELP("help", "Listet alle verfügbaren Kommandos mit Beschreibung auf", Collections.emptyList()),

    DUMMY("","",Collections.emptyList()); // Dummy command for not found commands

    private final String name;
    private final String description;
    private final List<CommandOption> options;

    private static final Logger logger = LoggerFactory.getLogger(Commands.class);
    private static final Pattern commandPattern = Pattern.compile("!(?<command>\\w+)\\s*(?<parameters>.*)");

    Commands(String name, String description, List<CommandOption> options) {
        this.name = name;
        this.description = description;
        this.options = options;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public List<CommandOption> getOptions() {
        return options;
    }

    public CommandData createCommandData() {
        CommandData command = new CommandData(name, description);
        for (CommandOption commandOption : options) {
            command.addOption(commandOption.getOptionType(), commandOption.getName(), commandOption.getDescription(), commandOption.isRequired());
        }
        return command;
    }

    public static Optional<Commands> findCommandForEvent(SlashCommandEvent event) {
        return Arrays.stream(Commands.values()).filter(command -> command.getName().equals(event.getName())).findFirst();
    }

    public static Optional<Commands> findCommandForEvent(MessageReceivedEvent event) {
        logger.debug("Finding Command for message: {}", event.getMessage().getContentRaw());
        Matcher commandMatcher = commandPattern.matcher(event.getMessage().getContentRaw());
        if (commandMatcher.matches()) {
            String commandStr = commandMatcher.group("command");
            return Arrays.stream(Commands.values()).filter(command -> command.getName().equals(commandStr)).findFirst();
        }
        return Optional.empty();
    }

    public static Optional<String> getParametersForCommand(String command) {
        Matcher commandMatcher = commandPattern.matcher(command);
        if (commandMatcher.matches()) {
            return Optional.ofNullable(commandMatcher.group("parameters"));
        }
        return Optional.empty();
    }

}
