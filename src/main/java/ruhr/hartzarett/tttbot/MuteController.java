package ruhr.hartzarett.tttbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ruhr.hartzarett.tttbot.data.Player;
import ruhr.hartzarett.tttbot.service.MuteService;

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
        muteService.muteEveryone(false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/mute")
    public ResponseEntity<Object> mute(@RequestBody Player player) {
//        Player player = new Player(name);
        logger.info("Got call to mute " + player);
        muteService.mute(player, true, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/unmute")
    public ResponseEntity<Object> unmute(@RequestBody Player player) {
//        Player player = new Player(name);
        logger.info("Got call to unmute " + player);
        muteService.mute(player, false, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
