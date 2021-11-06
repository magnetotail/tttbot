package ruhr.hartzarett.tttbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MuteController {

    private final Logger logger = LoggerFactory.getLogger(MuteController.class);

    private final MuteService muteService;

    public MuteController(MuteService muteService) {
        this.muteService = muteService;
    }

    @PostMapping(path = "/unmute/all")
    public ResponseEntity<Object> unmuteAll() {
        logger.info("Got call to unmute all");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        muteService.muteEveryone(false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/mute", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> mute(Player player) {
        logger.info("Got call to mute " + player);
        muteService.mute(player, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/unmute", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> unmute(Player player) {
        logger.info("Got call to unmute " + player);
        muteService.mute(player, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
