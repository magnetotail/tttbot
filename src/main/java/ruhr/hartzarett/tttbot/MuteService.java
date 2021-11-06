package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MuteService {

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
            Member member = registrationService.getMemberForPlayer(player);
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

    public void muteEveryone(boolean muted) {
        registrationService.getAllPlayers().forEach(p -> mute(p, muted));
    }
}
