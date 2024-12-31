package ruhr.hartzarett.tttbot.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ruhr.hartzarett.tttbot.data.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MuteService extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(MuteService.class);

    private final RegistrationService registrationService;
    private final JDAService jdaService;

    public MuteService(RegistrationService registrationService, JDAService jdaService) {
        this.registrationService = registrationService;
        this.jdaService = jdaService;
    }

    public void mute(Player player, boolean muteStatus, boolean everyone) {
        logger.info("Trying to {} player {}", muteStatus ? "mute" : "unmute", player);
        if (!registrationService.isRegistered(player)) {
            logger.warn("Could not find a discord user for steam name {}", player);
            jdaService.sendMessage("Konnte niemanden finden der zum Steamnamen " + player.getName() + "registriert ist.");
            return;
        }

        List<Member> members = registrationService.findMemberForPlayer(player);
        if (members.size() > 1) {
            logger.warn("Tried to mute {} but found more than one member with registered nickname. Members: {}", player.getName(), members);
            jdaService.sendMessage("Ich wollte " + player + " muten habe aber " + members + " gefunden. Nicht sicher wer hier richtig ist.");
            return;
        }
        if (members.isEmpty()) {
            logger.warn("Tried to mute {} but could not find any matching members in registry!", player.getName());
            jdaService.sendMessage("Ich wollte " + player + " muten habe aber niemand passenden gefunden.");
            return;
        }

        Member member = members.get(0);
        logger.info("Trying to {} member {}", muteStatus ? "mute" : "unmute", member.getEffectiveName());
        if (member.getVoiceState() == null || !member.getVoiceState().inVoiceChannel()) {
            logger.warn("Tried to {} {} but they are not in a voice channel as it seems", muteStatus ? "mute" : "unmute", member.getEffectiveName());
            return;
        }

        if (muteStatus || everyone) {
            member.mute(muteStatus).queue();
            logger.info("{} {}", muteStatus ? "muted" : "unmuted", member.getEffectiveName());
        } else {
            member.mute(false).queueAfter(3, TimeUnit.SECONDS); // unmute only used in case of disconnect. Server will send mute because of death before unmute so we wait...
            logger.info("Unmuting {} in 3 seconds.", member.getEffectiveName());
        }
    }

    @Async
    public void muteEveryone(boolean muted) {
        logger.info("Going to unmute everyone in one second. Waiting...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        registrationService.getAllPlayers().forEach(p -> mute(p, muted, true));
    }

}
