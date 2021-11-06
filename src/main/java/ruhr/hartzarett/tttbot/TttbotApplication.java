package ruhr.hartzarett.tttbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.security.auth.login.LoginException;

@SpringBootApplication
@EnableAsync
public class TttbotApplication {

    public static void main(String[] args) throws LoginException {
        SpringApplication.run(TttbotApplication.class, args);
    }
}
