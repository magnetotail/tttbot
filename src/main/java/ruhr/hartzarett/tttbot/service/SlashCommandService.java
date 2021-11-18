package ruhr.hartzarett.tttbot.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ruhr.hartzarett.tttbot.commands.CommandOptions;
import ruhr.hartzarett.tttbot.commands.Commands;
import ruhr.hartzarett.tttbot.data.Config;
import ruhr.hartzarett.tttbot.data.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SlashCommandService extends ListenerAdapter {


    private final Logger logger = LoggerFactory.getLogger(SlashCommandService.class);
    private final Config config;
    private final RegistrationService registrationService;
    private final ResourceBundle messageBundle;

    @Autowired
    public SlashCommandService(Config config, RegistrationService registrationService) {
        this.config = config;
        this.registrationService = registrationService;
        messageBundle = ResourceBundle.getBundle("messages", config.getLocale());
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        switch (Commands.findCommandForEvent(event).orElseThrow(() -> new RuntimeException("Unsupported Command: " + event))) {
            case REGISTER -> register(event);
            case HELP -> printHelp(event);
            case LIST -> listRegisteredMembers(event);
            case SHOW -> showNameForDiscordMember(event);
            case SHOW_FOR_STEAMNAME -> showNameForSteamUser(event);
            case REMOVE -> removeUser(event);
        }
    }

    private void showNameForSteamUser(@NotNull SlashCommandEvent event) {
        String steamName = Objects.requireNonNull(event.getOption(CommandOptions.STEAMNAME_REQUIRED.getName())).getAsString();
        Player toLookFor = new Player(steamName);
        List<Member> members = registrationService.findMemberForPlayer(toLookFor);
        String message = String.format(messageBundle.getString("currently_registered_for_steam"), toLookFor, !members.isEmpty() ? members.stream().map(Member::getEffectiveName).collect(Collectors.joining()) : messageBundle.getString("nobody"));
        event.reply(message).queue();
    }

    private void removeUser(@NotNull SlashCommandEvent event) {
        Objects.requireNonNull(event.getMember());
        if (registrationService.removeMember(event.getMember())) {
            logger.info("Removed user {}", event.getMember().getEffectiveName());
            event.reply(messageBundle.getString("was_removed")).queue();
        } else {
            logger.info("User {} was not registered", event.getMember().getEffectiveName());
            event.reply(messageBundle.getString("was_not_registered")).queue();
        }

    }

    private void showNameForDiscordMember(@NotNull SlashCommandEvent event) {
        Optional<Player> player = registrationService.findPlayerForMember(Objects.requireNonNull(event.getMember()));
        event.reply(String.format(messageBundle.getString("currently_registered_for_discord"), event.getMember().getEffectiveName(), player.isPresent() ? player.get() : messageBundle.getString("nobody"))).queue();
    }

    private void listRegisteredMembers(@NotNull SlashCommandEvent event) {
        event.reply(String.format(messageBundle.getString("currently_registered"), registrationService.getPlayerMap())).queue();
    }

    private void register(@NotNull SlashCommandEvent event) {
        String steamName = Objects.requireNonNull(event.getOption(CommandOptions.STEAMNAME_REQUIRED.getName())).getAsString();
        Player player = new Player(steamName);
        Member member = Objects.requireNonNull(event.getMember());
        registrationService.register(event.getMember(), player);
        logger.info("Registered user {} with player {}", member.getEffectiveName(), player);
        event.reply(String.format(messageBundle.getString("registeredUser"), player, member.getEffectiveName())).queue();
    }

    private void printHelp(SlashCommandEvent event) {
        event.reply(createHelpText()).queue();
    }

    private String createHelpText() {
        return messageBundle.getString("help_slash_command");
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
    }
}
