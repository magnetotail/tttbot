package ruhr.hartzarett.tttbot.commands;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public interface CommandOption {

    OptionType getOptionType();
    String getName();
    String getDescription();
    boolean isRequired();

    default String createHelpText() {
        return getName() + ": " + getDescription() + ". Pflichtparameter: " + (isRequired() ? "ja" : "nein");
    }
}
