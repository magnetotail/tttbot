package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.security.auth.login.LoginException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class JDAServiceTest {

    Config config;


    @BeforeEach
    void setUp() throws LoginException {
        config = new Config("","", false, 0, 0);
    }



    @Nested
    class Testing_Constructors {

        @Test
        void constructor_explodes_when_more_than_one_channel_found(@Mock JDA jda, @Mock TextChannel channel) {
            //arrange
            when(jda.getTextChannelsByName(config.getChannelName(), false))
                    .thenReturn(List.of(channel, channel));
            //Act
            Assertions.assertThatThrownBy(() -> new JDAService(config, jda))
                    //Assert
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void constructor_explodes_when_no_channel_found(@Mock JDA jda) {
            //arrange
            when(jda.getTextChannelsByName(config.getChannelName(), false))
                    .thenReturn(List.of());
            //Act
            Assertions.assertThatThrownBy(() -> new JDAService(config, jda), "Testing if constructor throws exception on empty channels")
                    //Assert
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void constructor_is_fine(@Mock JDA jda, @Mock TextChannel channel) {
            //arrange
            when(jda.getTextChannelsByName(config.getChannelName(), false))
                    .thenReturn(List.of(channel));
            //Act
            new JDAService(config, jda);
            //Assert
        }
    }


//    @Test
//    void addEventListener() {
//    }
//
//    @Test
//    void sendMessage() {
//    }
//
//    @Test
//    void reactToMessageWithOK() {
//    }
//
//    @Test
//    void reactToMessageWithAngryFace() {
//    }
//
//    @Test
//    void reactToMessage() {
//    }

}