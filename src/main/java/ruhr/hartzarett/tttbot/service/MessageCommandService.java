package ruhr.hartzarett.tttbot.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ruhr.hartzarett.tttbot.commands.Commands;
import ruhr.hartzarett.tttbot.data.Config;
import ruhr.hartzarett.tttbot.data.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
public class MessageCommandService extends ListenerAdapter {

    private final RegistrationService registrationService;
    private final JDAService jdaService;
    private final Config config;
    private final ResourceBundle messageBundle;

    @Autowired
    public MessageCommandService(RegistrationService registrationService, JDAService jdaService, Config config) {
        this.registrationService = registrationService;
        this.jdaService = jdaService;
        this.config = config;
        messageBundle = ResourceBundle.getBundle("messages", config.getLocale());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getName().equals(config.getChannelName()) && !event.getAuthor().isBot()) {
            switch (Commands.findCommandForEvent(event).orElse(Commands.DUMMY)) {
                case REGISTER -> register(event);
                case REMOVE -> remove(event);
                case SHOW_FOR_STEAMNAME -> showForSteam(event);
                case SHOW -> showForMember(event);
                case LIST -> list(event);
                case HELP -> printHelp(event);
                default -> {}
            }
        }
        super.onMessageReceived(event);
    }

    private void printHelp(MessageReceivedEvent event) {
        jdaService.sendMessage(messageBundle.getString("help_message_command"));
    }

    private void list(MessageReceivedEvent event) {
        jdaService.sendMessage(String.format(messageBundle.getString("currently_registered"), registrationService.getPlayerMap()));
    }

    private void showForMember(MessageReceivedEvent event) {
        Optional<Player> player = registrationService.findPlayerForMember(event.getMember());
        jdaService.sendMessage(String.format(messageBundle.getString("currently_registered_for_discord"), event.getMember().getEffectiveName(), player.isPresent() ? player.get() : messageBundle.getString("nobody")));

    }

    private void showForSteam(MessageReceivedEvent event) {
        Optional<String> steamName = Commands.getParametersForCommand(event.getMessage().getContentRaw());
        if (steamName.isEmpty()) {
            jdaService.sendMessage(messageBundle.getString("need_param_steamname"));
            return;
        }
        List<Member> members = registrationService.findMemberForPlayer(new Player(steamName.get()));
        jdaService.sendMessage(String.format(messageBundle.getString("currently_registered_for_steam"), steamName.get(), !members.isEmpty() ? members.stream().map(Member::getEffectiveName).collect(Collectors.joining()) : messageBundle.getString("nobody")));
    }

    private void remove(MessageReceivedEvent event) {
        if (!registrationService.isRegistered(event.getMember())) {
            jdaService.sendMessage(messageBundle.getString("was_not_registered"));
            jdaService.reactToMessageWithAngryFace(event.getMessage());
            return;
        }
        registrationService.removeMember(event.getMember());
        jdaService.sendMessage(messageBundle.getString("removed"));
        jdaService.reactToMessageWithOK(event.getMessage());
    }

    private void register(MessageReceivedEvent event) {
        Optional<String> possiblePlayer = Commands.getParametersForCommand(event.getMessage().getContentRaw());
        if (possiblePlayer.isEmpty()) {
            jdaService.sendMessage(messageBundle.getString("could_not_register_no_name"));
        }
        Player player = new Player(possiblePlayer.get());
        registrationService.register(event.getMember(), player);
        jdaService.sendMessage(String.format(messageBundle.getString("registered_user"), player.getName(), event.getMember().getEffectiveName()));
        jdaService.reactToMessageWithOK(event.getMessage());
    }
}
