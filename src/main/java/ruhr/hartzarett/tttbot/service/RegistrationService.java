package ruhr.hartzarett.tttbot.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ruhr.hartzarett.tttbot.data.Config;
import ruhr.hartzarett.tttbot.data.Player;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final Map<Member, Player> players;

    private final JDAService jdaService;

    private final Config config;

    private final ResourceBundle messageBundle;

    public RegistrationService(JDAService jdaService, Config config) {
        this.jdaService = jdaService;
        this.config = config;
        messageBundle = ResourceBundle.getBundle("messages", config.getLocale());
        players = new HashMap<>();
    }

    @EventListener(ApplicationReadyEvent.class)
    private void initialize() {
        logger.info("Listening to {}", config.getChannelName());
        jdaService.sendMessage(messageBundle.getString("greeting"));
    }

    public void register(@NotNull Member member, @NotNull Player player) {
        players.put(member, player);
        logger.debug("Registered user {} with player {}", member.getEffectiveName(), player);
    }

    public boolean removeMember(@NotNull Member member) {
        logger.debug("got call to remove {}", member.getEffectiveName());
        return players.remove(member) != null;
    }

    public Optional<Player> findPlayerForMember(@NotNull Member member) {
        return Optional.ofNullable(players.get(member));
    }

    public List<Member> findMemberForPlayer(@NotNull Player player) {
        return players.entrySet().stream().filter(e -> e.getValue().equals(player)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public boolean isRegistered(Player player) {
        return players.containsValue(player);
    }

    public boolean isRegistered(Member member) {
        return players.containsKey(member);
    }

    public Collection<Player> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public Map<Member, Player> getPlayerMap() {
        return new HashMap<>(players);
    }

}
