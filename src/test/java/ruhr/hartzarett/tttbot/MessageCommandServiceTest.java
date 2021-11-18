package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ruhr.hartzarett.tttbot.commands.Commands;
import ruhr.hartzarett.tttbot.data.Config;
import ruhr.hartzarett.tttbot.data.Player;
import ruhr.hartzarett.tttbot.service.JDAService;
import ruhr.hartzarett.tttbot.service.MessageCommandService;
import ruhr.hartzarett.tttbot.service.RegistrationService;
import ruhr.hartzarett.tttbot.util.TextKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class MessageCommandServiceTest {

    private static final String CHANNEL_NAME = "ttt-bot";
    private static final String MEMBER_NAME_DEFAULT = "DiscordUser";
    private static final String STEAM_NAME_DEFAULT = "Steamname";
    private static final String MEMBER_NAME_WITH_SPACE = "with space";
    private static final String STEAM_NAME_WITH_SPACE = "steam with space";
    private MessageCommandService testee;
    private RegistrationService registrationService;
    private JDAService jdaService;
    private ResourceBundle messageBundle;

    @BeforeEach
    void setUp(@Mock JDAService jdaService) {
        Config config = new Config(CHANNEL_NAME, "testToken", false, 0, 0, "de_DE");
        this.jdaService = jdaService;
        registrationService = new RegistrationService(jdaService, config);
        testee = new MessageCommandService(registrationService, jdaService, config);
        messageBundle = ResourceBundle.getBundle("messages", config.getLocale());
    }

    void triggerMessageReceivedEvent(Message message, Member member, String memberName, MessageCommandService service) {
        User author = mock(User.class);
        when(author.isBot()).thenReturn(false);

        MessageChannel channel = mock(MessageChannel.class);
        when(channel.getName()).thenReturn(CHANNEL_NAME);

        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getMessage()).thenReturn(message);
        when(event.getChannel()).thenReturn(channel);
        when(event.getAuthor()).thenReturn(author);
        when(event.getMember()).thenReturn(member);

        service.onMessageReceived(event);
    }

    void triggerMessageReceivedEvent(Message message, Member member, String memberName) {
        triggerMessageReceivedEvent(message, member, memberName, testee);
    }

    void triggerMessageReceivedEvent(Message message, String memberName, MessageCommandService service) {
        User author = mock(User.class);
        when(author.isBot()).thenReturn(false);

        MessageChannel channel = mock(MessageChannel.class);
        when(channel.getName()).thenReturn(CHANNEL_NAME);

        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getMessage()).thenReturn(message);
        when(event.getChannel()).thenReturn(channel);
        when(event.getAuthor()).thenReturn(author);

        service.onMessageReceived(event);
    }

    @Nested
    class Test_Message_Received_Event {

        @Nested
        class Register_User {

            @Test
            void register_user(@Mock Message message, @Mock Member member) {
                //arrange
                when(message.getContentRaw()).thenReturn("!" + Commands.REGISTER.getName() + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(message, member, MEMBER_NAME_DEFAULT);
                //assert
                assertThat(registrationService.getAllPlayers().size()).isEqualTo(1);
                assertThat(registrationService.findMemberForPlayer(new Player(STEAM_NAME_DEFAULT)).get(0).getEffectiveName()).isEqualTo(MEMBER_NAME_DEFAULT);
                assertThat(registrationService.findMemberForPlayer(new Player(STEAM_NAME_DEFAULT)).get(0)).isEqualTo(member);
                Mockito.verify(jdaService).sendMessage(String.format(messageBundle.getString(TextKeys.MESSAGE_REGISTERED), new Player(STEAM_NAME_DEFAULT), member.getEffectiveName()));
                Mockito.verify(jdaService).reactToMessageWithOK(message);
            }
        }

        @Nested
        class Show_For_SteamName {

            @Test
            void show_user_for_steamname(@Mock Message messageRegister, @Mock Member member, @Mock Message messageShow) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn("!" + Commands.REGISTER.getName() + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                when(messageShow.getContentRaw()).thenReturn("!" + Commands.SHOW_FOR_STEAMNAME.getName() + " " + STEAM_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(messageShow, MEMBER_NAME_DEFAULT, testee);
                //assert
                verify(jdaService).sendMessage(String.format(messageBundle.getString(TextKeys.MESSAGE_REGISTERED_FOR_STEAM), STEAM_NAME_DEFAULT, MEMBER_NAME_DEFAULT));
            }

            @Test
            void show_user_for_not_registered_steamname(@Mock Message messageShow) {
                //arrange
                when(messageShow.getContentRaw()).thenReturn("!" + Commands.SHOW_FOR_STEAMNAME.getName() + " " + STEAM_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(messageShow, MEMBER_NAME_DEFAULT, testee);
                //assert
                verify(jdaService).sendMessage(String.format(messageBundle.getString(TextKeys.MESSAGE_REGISTERED_FOR_STEAM), STEAM_NAME_DEFAULT, "niemand"));
            }
        }

        @Nested
        class Show_For_Discord_User {

            @Test
            void show_steamname_for_user(@Mock Message messageRegister, @Mock Member member, @Mock Message messageShow) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn("!" + Commands.REGISTER.getName() + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                when(messageShow.getContentRaw()).thenReturn("!" + Commands.SHOW.getName());
                //act
                triggerMessageReceivedEvent(messageShow, member, MEMBER_NAME_DEFAULT);
                //assert
                verify(jdaService).sendMessage(String.format(messageBundle.getString(TextKeys.MESSAGE_REGISTERED_FOR_DISCORD), MEMBER_NAME_DEFAULT, STEAM_NAME_DEFAULT));
            }

            @Test
            void show_steamname_for_not_registered_user(@Mock Member member, @Mock Message messageShow) {
                //arrange
                when(messageShow.getContentRaw()).thenReturn("!" + Commands.SHOW.getName());
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(messageShow, member, MEMBER_NAME_DEFAULT);
                //assert
                verify(jdaService).sendMessage(String.format(messageBundle.getString(TextKeys.MESSAGE_REGISTERED_FOR_DISCORD), MEMBER_NAME_DEFAULT, "niemand"));
            }
        }

        @Test
        void list_users_with_one_registered(@Mock Message messageRegister, @Mock Member member, @Mock Message messageList) {
            //arrange
            when(messageRegister.getContentRaw()).thenReturn("!" + Commands.REGISTER.getName() + " " + STEAM_NAME_DEFAULT);
            when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
            when(member.toString()).thenReturn(MEMBER_NAME_DEFAULT);
            triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
            when(messageList.getContentRaw()).thenReturn("!" + Commands.LIST.getName());

            Map<Member, Player> compareMap = new HashMap<>();
            compareMap.put(member, new Player(STEAM_NAME_DEFAULT));
            //act
            triggerMessageReceivedEvent(messageList, MEMBER_NAME_DEFAULT, testee);

            //assert
            verify(jdaService).sendMessage(String.format(messageBundle.getString(TextKeys.MESSAGE_CURRENTLY_REGISTERED), compareMap));
        }

        @Nested
        class Test_Is_Registered {

            @Test
            void steamname_registered(@Mock Message messageRegister, @Mock Member member) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn("!" + Commands.REGISTER.getName() + " " + STEAM_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                //act
                boolean isRegistered = registrationService.isRegistered(new Player(STEAM_NAME_DEFAULT));
                //assert
                assertThat(isRegistered).isTrue();
            }

            @Test
            void steamname_not_registered() {
                //arrange
                //act
                boolean isRegistered = registrationService.isRegistered(new Player(STEAM_NAME_DEFAULT));
                //assert
                assertThat(isRegistered).isFalse();
            }
        }

        @Nested
        class Remove {
            @Test
            void remove_existing_user(@Mock Message messageRegister, @Mock Message messageRemove, @Mock Member member) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn("!" + Commands.REGISTER.getName() + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                when(messageRemove.getContentRaw()).thenReturn("!" + Commands.REMOVE.getName());
                //act
                triggerMessageReceivedEvent(messageRemove, member, MEMBER_NAME_DEFAULT);
                //assert
                assertThat(registrationService.getAllPlayers().size()).isEqualTo(0);
                verify(jdaService).sendMessage(messageBundle.getString(TextKeys.MESSAGE_REMOVED));
                verify(jdaService).reactToMessageWithOK(messageRemove);
            }

            @Test
            void remove_not_existing_user(@Mock Message messageRemove, @Mock Member member) {
                //arrange
                when(messageRemove.getContentRaw()).thenReturn("!" + Commands.REMOVE.getName());
                //act
                triggerMessageReceivedEvent(messageRemove, member, MEMBER_NAME_DEFAULT);
                //assert
                assertThat(registrationService.getAllPlayers().size()).isEqualTo(0);
                verify(jdaService).sendMessage(messageBundle.getString(TextKeys.MESSAGE_WASNT_REGISTERED));
                verify(jdaService).reactToMessageWithAngryFace(messageRemove);
            }
        }

    }

//    @Test
//    void onMessageReceivedList() {
//
//    }
//
//    @Test
//    void onMessageReceivedShowForSteam() {
//
//    }
//
//    @Test
//    void onMessageReceivedShowForDiscord() {
//
//    }
//
//    @Test
//    void onMessageReceivedHelp() {
//
//    }
//
//    @Test
//    void getMemberForPlayer() {
//    }
//
//    @Test
//    void isRegistered() {
//    }
//
//    @Test
//    void getAllPlayers() {
//    }
}