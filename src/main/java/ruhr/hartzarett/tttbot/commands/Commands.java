package ruhr.hartzarett.tttbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum Commands implements Command {
    REGISTER("register", "Registriert dich", List.of(CommandOptions.STEAMNAME_REQUIRED)),
    REMOVE("remove", "Entfernt dich", Collections.emptyList()),
    SHOW_FOR_STEAMNAME("showforsteam", "Zeigt an, welcher Member mit einem Steamnamen verknüpft ist", List.of(CommandOptions.STEAMNAME_REQUIRED)),
    SHOW("show", "Zeigt an, welcher Steamname mit dir verknüpft ist", Collections.emptyList()),
    LIST("list", "Listet alle registrierten Member mit zugehörigem Steamnamen auf", Collections.emptyList()),
    HELP("help", "Listet alle verfügbaren Kommandos mit Beschreibung auf", Collections.emptyList());

    private final String name;
    private final String description;
    private final List<CommandOption> options;

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

}
