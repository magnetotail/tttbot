package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

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


    public static final String LIST_COMMAND = "!list";
    public static final String REGISTER_COMMAND = "!register";
    public static final String REMOVE_COMMAND = "!remove";
    public static final String SHOW_FOR_STEAM = "!forsteam";
    public static final String SHOW_COMMAND = "!show";

    public static final String HELP_TEXT = "Es scheint, dass du nicht ganz genau weißt, wie das hier funktioniert, aber kein Problem :)\n" +
            "Ich kann deine TTT-Experience unterstützen, indem ich dich automatisch mute, wenn du im Spiel stirbst. Am Ende der Runde wirst du automatisch entmutet.\n" +
            "Das funktioniert so:\n" +
            REGISTER_COMMAND + " {DEIN STEAM NAME} registriert deinen Steam ingamenamen, wodurch erkannt wird, dass du gestorben bist.\n" +
            REMOVE_COMMAND + " Löscht automatisch die Verbindung mit deinem Namen\n" +
            LIST_COMMAND + " Listet alle aktuellen Verbindungen auf\n" +
            SHOW_FOR_STEAM + " {DEIN STEAM NAME} Zeigt, welcher Discord Account für einen Steamnamen verknüpft ist\n" +
            SHOW_COMMAND + " Zeigt an, welche steamnamen mit deinem Account verknüpft sind\n";

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

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getName().equals(config.getChannelName()) && !event.getAuthor().isBot()) {
            logger.info("Got Discord Message: {} from {}", event.getMessage().getContentRaw(), event.getAuthor().getName());
            if (event.getMessage().getContentRaw().startsWith(REGISTER_COMMAND)) {
                register(event);
            } else if (event.getMessage().getContentRaw().startsWith(REMOVE_COMMAND)) {
                removeUser(event);
            } else if (event.getMessage().getContentRaw().startsWith(LIST_COMMAND)) {
                listRegisteredMembers(event);
            } else if (event.getMessage().getContentRaw().startsWith(SHOW_FOR_STEAM)) {
                showNameForSteamUser(event);
            } else if (event.getMessage().getContentRaw().startsWith(SHOW_COMMAND)) {
                showNameForDiscordMember(event);
            } else {
                printHelp(event);
            }
        }
    }

    private void register(@NotNull MessageReceivedEvent event) {
        Player player = new Player(event.getMessage().getContentRaw().substring(REGISTER_COMMAND.length()).trim());
        Member member = event.getMember();
        players.put(event.getMember(), player);
        logger.info("Registered user {} with player {}", member.getEffectiveName(), player);
        if (config.isFunnyEnabled() && ThreadLocalRandom.current().nextInt(0, 100) < config.getTrollPercentage()) {
            jdaService.reactToMessage(event.getMessage(), "U+1F92A");
            jdaService.sendMessage(TROLL_ANSWER_REGISTER);
            try {
                Thread.sleep(config.getTrollWaittime());
            } catch (InterruptedException e) {
            }
            event.getMessage().reply("Spaß, hab dich registriert :P").queue();
        } else {
            jdaService.reactToMessageWithOK(event.getMessage());
            jdaService.sendMessage(String.format(FORMAT_STRING_REGISTERED_USER, player, member.getEffectiveName()));
        }
    }

    private void removeUser(@NotNull MessageReceivedEvent event) {
        Player removed = players.remove(event.getMember());
        if (removed != null) {
            logger.info("Removed user {}", event.getMember().getEffectiveName());
            jdaService.reactToMessageWithOK(event.getMessage());
            jdaService.sendMessage(MESSAGE_REMOVED);
        } else {
            logger.info("User {} was not registered", event.getMember().getEffectiveName());
            jdaService.reactToMessageWithAngryFace(event.getMessage());
            jdaService.sendMessage(WAS_NOT_REGISTERED);
        }

    }

    private void showNameForDiscordMember(@NotNull MessageReceivedEvent event) {
        Player registeredPlayer = players.get(event.getMember());
        String message = String.format(FORMAT_STRING_CURRENTLY_REGISTERED_FOR_DISCORD, event.getMember().getEffectiveName(), registeredPlayer != null ? registeredPlayer : "nichts");
        jdaService.sendMessage(message);
    }

    private void listRegisteredMembers(@NotNull MessageReceivedEvent event) {
       jdaService.sendMessage(String.format(CURRENTLY_REGISTERED, players.toString()));
    }

    private void showNameForSteamUser(@NotNull MessageReceivedEvent event) {
        Player toLookFor = new Player(event.getMessage().getContentRaw().substring(REGISTER_COMMAND.length()).trim());
        String foundUsers = players.entrySet().stream().filter(entry -> entry.getValue().equals(toLookFor)).map(p -> p.getKey().getEffectiveName()).collect(Collectors.joining(", "));
        String message = String.format(FORMAT_STRING_CURRENTLY_REGISTERED_FOR_STEAMNAME, toLookFor, foundUsers.length() > 0 ? foundUsers : "nichts");
        jdaService.sendMessage(message);
    }

    public Member getMemberForPlayer(Player player) {
        return players.entrySet().stream().filter(entry -> entry.getValue().equals(player)).findFirst().get().getKey();
    }

    public boolean isRegistered(Player player) {
        return players.containsValue(player);
    }

    private void printHelp(MessageReceivedEvent event) {
        jdaService.sendMessage(createHelpText());
    }

    private String createHelpText() {
        return HELP_TEXT;
    }

    public Collection<Player> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }
}
