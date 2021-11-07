package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JDAService {

    Logger logger = LoggerFactory.getLogger(JDAService.class);

    private JDA jda;

    private TextChannel channel;

    @Autowired
    public JDAService(Config config, JDA jda) {
        logger.info("Initializing JDA");
        this.jda = jda;
        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("JDA Initialization succesful!");
        logger.info("Searching for channels with name {}", config.getChannelName());
        List<TextChannel> channels = this.jda.getTextChannelsByName(config.getChannelName(), false);
        if (channels.isEmpty()) {
            logger.error("Could not find the specified channel {}. Please check the name in your application.properties file. Exiting.", config.getChannelName());
            throw new RuntimeException("Could not find the channel I was supposed to look for");
        }
        if (channels.size() > 1) {
            logger.error("Found more than one channel with name {}. Exiting, because I don't know which one to use. Please use a unique channel name.", config.getChannelName());
            throw new RuntimeException("Found more than one channel. Please specify a unique channel name");
        }
        channel = channels.get(0);
        logger.info("Found channel {}", config.getChannelName());
    }

    public void addEventListener(EventListener listener) {
        jda.addEventListener(listener);
    }

    public void sendMessage(String message) {
        logger.info("Sending message \"{}\"", message);
        channel.sendMessage(message).queue();
    }

    public void reactToMessageWithOK(Message message) {
        reactToMessage(message, "U+1F44C");
    }

    public void reactToMessageWithAngryFace(Message message) {
        reactToMessage(message, "U+1F621");
    }

    public void reactToMessage(Message message, String unicodeEmote) {
        message.addReaction(unicodeEmote).queue();
    }

}
