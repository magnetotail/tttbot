package ruhr.hartzarett.tttbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    private final String channelName;
    private final String botToken;
    private final boolean isFunnyEnabled;

    public Config(@Value("${discord.channelname}") String channelName, @Value("${discord.token}") String botToken,@Value("${discord.beFunny}") boolean isFunnyEnabled) {
        this.channelName = channelName;
        this.botToken = botToken;
        this.isFunnyEnabled = isFunnyEnabled;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getChannelName() {
        return channelName;
    }

    public boolean isFunnyEnabled() {
        return isFunnyEnabled;
    }


}
