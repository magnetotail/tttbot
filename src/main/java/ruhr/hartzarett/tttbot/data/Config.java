package ruhr.hartzarett.tttbot.data;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotEmpty;
import java.util.Locale;


@ConfigurationProperties(prefix = "discord")
@ConstructorBinding
public class Config {

    @NotEmpty
    private final String channelName;
    @NotEmpty
    private final String botToken;
    private final boolean isFunnyEnabled;
    private final int trollPercentage;
    private final int trollWaittime;
    private final Locale locale;

    private Logger logger = LoggerFactory.getLogger(Config.class);

    public Config(String channelName, String token, boolean beFunny, int trollPercentage, int trollWaitTime, String locale) {
        if (trollPercentage < 0 || trollPercentage > 100)
            throw new IllegalArgumentException("trollPercentage muss zwischen 0 und 100 liegen!");
        this.channelName = channelName;
        this.botToken = token;
        this.isFunnyEnabled = beFunny;
        this.trollPercentage = trollPercentage;
        this.trollWaittime = trollWaitTime;
        this.locale = Locale.GERMAN;
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

    public int getTrollWaittime() {
        return trollWaittime;
    }

    public int getTrollPercentage() {
        return trollPercentage;
    }

    @Bean
    public JDA getJDABuilder() throws LoginException {
        try {
            return JDABuilder.createDefault(botToken).build();
        } catch (LoginException e) {
            logger.error("It seems the token did not work or something else prohibited us from logging into Discord API", e);
            logger.error("Exiting, running this application without connection to Discord API is useless.");
            throw e;
        }
    }

    public Locale getLocale() {
        return locale;
    }
}
