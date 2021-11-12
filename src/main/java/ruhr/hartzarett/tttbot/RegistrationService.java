package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ruhr.hartzarett.tttbot.commands.CommandOptions;
import ruhr.hartzarett.tttbot.commands.Commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
class RegistrationService extends ListenerAdapter {

    public static final String FORMAT_STRING_CURRENTLY_REGISTERED_FOR_DISCORD = "Aktuell registriert für User %s: %s";
    public static final String FORMAT_STRING_CURRENTLY_REGISTERED_FOR_STEAMNAME = "Aktuell registriert für Steam Namen %s: %s";
    public static final String FORMAT_STRING_REGISTERED_USER = "Habe den Steamnamen \"%s\" mit dem Discordnamen \"%s\" verknüpft";
    public static final String MESSAGE_REMOVED = "Ich habe dich aus der Liste entfernt :)";


    public static final String LIST_COMMAND = "/list";
    public static final String REGISTER_COMMAND = "!register";
    public static final String REMOVE_COMMAND = "!remove";
    public static final String SHOW_FOR_STEAM = "!forsteam";
    public static final String SHOW_COMMAND = "!show";

    public static final String HELP_TEXT = "Es scheint, dass du nicht ganz genau weißt, wie das hier funktioniert, aber kein Problem :)\n" +
            "Ich kann deine TTT-Experience unterstützen, indem ich dich automatisch mute, wenn du im Spiel stirbst. Am Ende der Runde wirst du automatisch entmutet.\n" +
            "Das funktioniert so:\n" +
            "/" + Commands.REGISTER.getName() + " {DEIN STEAM NAME} registriert deinen Steam ingamenamen, wodurch erkannt wird, dass du gestorben bist.\n" +
            "/" + Commands.REMOVE.getName() + " Löscht automatisch die Verbindung mit deinem Namen\n" +
            "/" + Commands.LIST.getName() + " Listet alle aktuellen Verbindungen auf\n" +
            "/" + Commands.SHOW_FOR_STEAMNAME.getName() + " {DEIN STEAM NAME} Zeigt, welcher Discord Account für einen Steamnamen verknüpft ist\n" +
            "/" + Commands.SHOW.getName() + " Zeigt an, welche steamnamen mit deinem Account verknüpft sind\n";

    public static final String GREETING = "Ich wurde neugestartet. Bitte registriert euch erneut, falls ihr automatisch gemutet werden wollt :)";
    public static final String WAS_NOT_REGISTERED = "Du warst gar nicht registriert";
    public static final String TROLL_ANSWER_REGISTER = "Nööööö, du nicht..";
    public static final String CURRENTLY_REGISTERED = "Aktuell registriert: \n%s";

    private static Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final Map<Member, Player> players;

    private final JDAService jdaService;

    private final Config config;

    public RegistrationService(JDAService jdaService, Config config) {
        this.jdaService = jdaService;
        this.config = config;
        players = new HashMap<>();
    }

    @EventListener(ApplicationReadyEvent.class)
    private void initialize() {
        jdaService.addEventListener(this);
        logger.info("Listening to {}", config.getChannelName());
        jdaService.sendMessage(GREETING);
    }


    private void register(@NotNull SlashCommandEvent event) {
        Player player = new Player(event.getOption(CommandOptions.STEAMNAME_REQUIRED.getName()).getAsString());
        Member member = event.getMember();
        players.put(event.getMember(), player);
        logger.info("Registered user {} with player {}", member.getEffectiveName(), player);
        if (config.isFunnyEnabled() && ThreadLocalRandom.current().nextInt(0, 100) < config.getTrollPercentage()) {
            jdaService.sendMessage(TROLL_ANSWER_REGISTER);
            try {
                Thread.sleep(config.getTrollWaittime());
            } catch (InterruptedException e) {
            }
        } else {
            event.reply(String.format(FORMAT_STRING_REGISTERED_USER, player, member.getEffectiveName())).queue();
        }
    }

    private void removeUser(@NotNull SlashCommandEvent event) {
        Player removed = players.remove(event.getMember());
        if (removed != null) {
            logger.info("Removed user {}", event.getMember().getEffectiveName());
            event.reply(MESSAGE_REMOVED).queue();
        } else {
            logger.info("User {} was not registered", event.getMember().getEffectiveName());
            event.reply(WAS_NOT_REGISTERED).queue();
        }

    }

    private void showNameForDiscordMember(@NotNull SlashCommandEvent event) {
        Player registeredPlayer = players.get(event.getMember());
        String message = String.format(FORMAT_STRING_CURRENTLY_REGISTERED_FOR_DISCORD, event.getMember().getEffectiveName(), registeredPlayer != null ? registeredPlayer : "nichts");
        event.reply(message).queue();
    }

    private void listRegisteredMembers(@NotNull SlashCommandEvent event) {
        event.reply(String.format(CURRENTLY_REGISTERED, players.toString())).queue();
    }

    private void showNameForSteamUser(@NotNull SlashCommandEvent event) {
        Player toLookFor = new Player(event.getOption(CommandOptions.STEAMNAME_REQUIRED.getName()).getAsString());
        String foundUsers = players.entrySet().stream().filter(entry -> entry.getValue().equals(toLookFor)).map(p -> p.getKey().getEffectiveName()).collect(Collectors.joining(", "));
        String message = String.format(FORMAT_STRING_CURRENTLY_REGISTERED_FOR_STEAMNAME, toLookFor, foundUsers.length() > 0 ? foundUsers : "nichts");
        event.reply(message).queue();
    }

    public Member getMemberForPlayer(Player player) {
        return players.entrySet().stream().filter(entry -> entry.getValue().equals(player)).findFirst().get().getKey();
    }

    public boolean isRegistered(Player player) {
        return players.containsValue(player);
    }

    private void printHelp(SlashCommandEvent event) {
        event.reply(createHelpText()).queue();
    }

    private String createHelpText() {
        return HELP_TEXT;
    }

    public Collection<Player> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
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

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getButton().getLabel().equals("unmute")) {
            System.out.println("DA WILL WOHL JEMAND UNMUTET WERDEN OLOLOLOLO");
            event.getChannel().sendMessage("Nö").queue();
        }
        event.reply("fooblubb").queue();
    }
}
