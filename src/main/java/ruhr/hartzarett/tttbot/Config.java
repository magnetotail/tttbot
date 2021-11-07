package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotEmpty;


@ConfigurationProperties(prefix = "discord")
@ConstructorBinding
@Validated
public class Config {

    Logger logger = LoggerFactory.getLogger(Config.class);

    @NotNull
    @NotEmpty
    private final String channelName;
    @NotNull
    @NotEmpty
    private final String botToken;
    @NotNull
    private final boolean isFunnyEnabled;

    public Config(String channelName, String token, boolean beFunny) {
        this.channelName = channelName;
        this.botToken = token;
        this.isFunnyEnabled = beFunny;
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

}
