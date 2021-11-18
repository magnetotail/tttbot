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

@Service
public class MuteService extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(MuteService.class);

    private final RegistrationService registrationService;
    private final JDAService jdaService;

    public MuteService(RegistrationService registrationService, JDAService jdaService) {
        this.registrationService = registrationService;
        this.jdaService = jdaService;
    }

    public void mute(Player player, boolean muteStatus) {
        logger.info("Trying to {} player {}", muteStatus ? "mute" : "unmute", player);
        if (registrationService.isRegistered(player)) {
            Member member = registrationService.findMemberForPlayer(player).get(0);
            logger.info("Trying to {} member {}", muteStatus ? "mute" : "unmute", member.getEffectiveName());
            if (member.getVoiceState().inVoiceChannel()) {
                member.mute(muteStatus).queue();
                logger.info("{} {}", muteStatus ? "Muted" : "Unmuted", member.getEffectiveName());
            } else {
                logger.warn("Tried to {} {} but they are not in a voice channel as it seems", muteStatus ? "mute" : "unmute", member.getEffectiveName());
            }
        } else {
            logger.warn("Could not find a discord user for steam name " + player);
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
        registrationService.getAllPlayers().forEach(p -> mute(p, muted));
    }

}
