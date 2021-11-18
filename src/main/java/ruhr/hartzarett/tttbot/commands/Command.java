package ruhr.hartzarett.tttbot.commands;

import java.util.List;

public interface Command {

    String getName();

    String getDescription();

    List<CommandOption> getOptions();

    default String createHelpText() {
        return getName() + ": " + getDescription() + ". Parameter: " + (!getOptions().isEmpty() ? getOptions().stream().map(CommandOption::createHelpText) : "keine.");
    }


}
