package ruhr.hartzarett.tttbot.commands;

import java.util.List;

public interface Command {

    String getName();

    String getDescription();

    List<CommandOption> getOptions();

}
