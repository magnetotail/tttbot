package ruhr.hartzarett.tttbot.commands;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public enum CommandOptions implements CommandOption {
    STEAMNAME_REQUIRED(OptionType.STRING, "steamname", "Anzeigename in Steam", true);

    private final OptionType optionType;
    private final String name;
    private final String description;
    private final boolean isRequired;

    CommandOptions(OptionType optionType, String name, String description, boolean isRequired) {
        this.optionType = optionType;
        this.name = name;
        this.description = description;
        this.isRequired = isRequired;
    }

    @Override
    public OptionType getOptionType() {
        return optionType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }
}
