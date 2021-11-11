package ruhr.hartzarett.tttbot;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final String CHANNEL_NAME = "ttt-bot";
    private static final String MEMBER_NAME_DEFAULT = "DiscordUser";
    private static final String STEAM_NAME_DEFAULT = "Steamname";
    private static final String MEMBER_NAME_WITH_SPACE = "with space";
    private static final String STEAM_NAME_WITH_SPACE = "steam with space";
    private RegistrationService testee;
    private JDAService jdaService;

    @BeforeEach
    void setUp(@Mock JDAService jdaService) {
        Config config = new Config(CHANNEL_NAME, "testToken", false, 0, 0);
        this.jdaService = jdaService;
        testee = new RegistrationService(jdaService, config);
    }

    void triggerMessageReceivedEvent(Message message, Member member, String memberName, RegistrationService service) {
        User author = mock(User.class);
        when(author.isBot()).thenReturn(false);
        when(author.getName()).thenReturn(memberName);

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

    void triggerMessageReceivedEvent(Message message, String memberName, RegistrationService service) {
        User author = mock(User.class);
        when(author.isBot()).thenReturn(false);
        when(author.getName()).thenReturn(memberName);

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
                when(message.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(message, member, MEMBER_NAME_DEFAULT);
                //assert
                assertThat(testee.getAllPlayers().size()).isEqualTo(1);
                assertThat(testee.getMemberForPlayer(new Player(STEAM_NAME_DEFAULT)).getEffectiveName()).isEqualTo(MEMBER_NAME_DEFAULT);
                assertThat(testee.getMemberForPlayer(new Player(STEAM_NAME_DEFAULT))).isEqualTo(member);
                Mockito.verify(jdaService).sendMessage(String.format(RegistrationService.FORMAT_STRING_REGISTERED_USER, new Player(STEAM_NAME_DEFAULT), member.getEffectiveName()));
                Mockito.verify(jdaService).reactToMessageWithOK(message);
            }

            @Test
            void troll_user_trying_to_register(@Mock Message messageRegister, @Mock Member member, @Mock MessageAction messageAction) {
                //arrange
                Config config = new Config(CHANNEL_NAME, "testToken", true, 100, 0);
                when(messageRegister.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                when(messageRegister.reply("Spaß, hab dich registriert :P")).thenReturn(messageAction);
                RegistrationService service = new RegistrationService(jdaService, config);
                //act
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT, service);
                //assert
                assertThat(service.getAllPlayers().size()).isEqualTo(1);
                assertThat(service.getMemberForPlayer(new Player(STEAM_NAME_DEFAULT)).getEffectiveName()).isEqualTo(MEMBER_NAME_DEFAULT);
                assertThat(service.getMemberForPlayer(new Player(STEAM_NAME_DEFAULT))).isEqualTo(member);
                Mockito.verify(jdaService).sendMessage(RegistrationService.TROLL_ANSWER_REGISTER);
                Mockito.verify(jdaService).reactToMessage(messageRegister, "U+1F92A");
            }
        }

        @Nested
        class Show_For_SteamName {

            @Test
            void show_user_for_steamname(@Mock Message messageRegister, @Mock Member member, @Mock Message messageShow) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                when(messageShow.getContentRaw()).thenReturn(RegistrationService.SHOW_FOR_STEAM + " " + STEAM_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(messageShow, MEMBER_NAME_DEFAULT, testee);
                //assert
                verify(jdaService).sendMessage(String.format(RegistrationService.FORMAT_STRING_CURRENTLY_REGISTERED_FOR_STEAMNAME, STEAM_NAME_DEFAULT, MEMBER_NAME_DEFAULT));
            }

            @Test
            void show_user_for_not_registered_steamname(@Mock Message messageShow) {
                //arrange
                when(messageShow.getContentRaw()).thenReturn(RegistrationService.SHOW_FOR_STEAM + " " + STEAM_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(messageShow, MEMBER_NAME_DEFAULT, testee);
                //assert
                verify(jdaService).sendMessage(String.format(RegistrationService.FORMAT_STRING_CURRENTLY_REGISTERED_FOR_STEAMNAME, STEAM_NAME_DEFAULT, "nichts"));
            }
        }

        @Nested
        class Show_For_Discord_User {

            @Test
            void show_steamname_for_user(@Mock Message messageRegister, @Mock Member member, @Mock Message messageShow) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                when(messageShow.getContentRaw()).thenReturn(RegistrationService.SHOW_COMMAND);
                //act
                triggerMessageReceivedEvent(messageShow, member, MEMBER_NAME_DEFAULT);
                //assert
                verify(jdaService).sendMessage(String.format(RegistrationService.FORMAT_STRING_CURRENTLY_REGISTERED_FOR_DISCORD, MEMBER_NAME_DEFAULT, STEAM_NAME_DEFAULT));
            }

            @Test
            void show_steamname_for_not_registered_user(@Mock Member member, @Mock Message messageShow) {
                //arrange
                when(messageShow.getContentRaw()).thenReturn(RegistrationService.SHOW_COMMAND);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                //act
                triggerMessageReceivedEvent(messageShow, member, MEMBER_NAME_DEFAULT);
                //assert
                verify(jdaService).sendMessage(String.format(RegistrationService.FORMAT_STRING_CURRENTLY_REGISTERED_FOR_DISCORD, MEMBER_NAME_DEFAULT, "nichts"));
            }
        }

        @Test
        void list_users_with_one_registered(@Mock Message messageRegister, @Mock Member member, @Mock Message messageList) {
            //arrange
            when(messageRegister.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
            when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
            when(member.toString()).thenReturn(MEMBER_NAME_DEFAULT);
            triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
            when(messageList.getContentRaw()).thenReturn(RegistrationService.LIST_COMMAND);

            Map<Member, Player> compareMap = new HashMap<>();
            compareMap.put(member, new Player(STEAM_NAME_DEFAULT));
            //act
            triggerMessageReceivedEvent(messageList, MEMBER_NAME_DEFAULT, testee);

            //assert
            verify(jdaService).sendMessage(String.format(RegistrationService.CURRENTLY_REGISTERED, compareMap));
        }

        @Nested
        class Test_Is_Registered {

            @Test
            void steamname_registered(@Mock Message messageRegister, @Mock Member member) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                //act
                boolean isRegistered = testee.isRegistered(new Player(STEAM_NAME_DEFAULT));
                //assert
                assertThat(isRegistered).isTrue();
            }

            @Test
            void steamname_not_registered() {
                //arrange
                //act
                boolean isRegistered = testee.isRegistered(new Player(STEAM_NAME_DEFAULT));
                //assert
                assertThat(isRegistered).isFalse();
            }
        }

        @Nested
        class Remove {
            @Test
            void remove_existing_user(@Mock Message messageRegister, @Mock Message messageRemove, @Mock Member member) {
                //arrange
                when(messageRegister.getContentRaw()).thenReturn(RegistrationService.REGISTER_COMMAND + " " + STEAM_NAME_DEFAULT);
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                triggerMessageReceivedEvent(messageRegister, member, MEMBER_NAME_DEFAULT);
                when(messageRemove.getContentRaw()).thenReturn(RegistrationService.REMOVE_COMMAND);
                //act
                triggerMessageReceivedEvent(messageRemove, member, MEMBER_NAME_DEFAULT);
                //assert
                assertThat(testee.getAllPlayers().size()).isEqualTo(0);
                verify(jdaService).sendMessage(RegistrationService.MESSAGE_REMOVED);
                verify(jdaService).reactToMessageWithOK(messageRemove);
            }

            @Test
            void remove_not_existing_user(@Mock Message messageRemove, @Mock Member member) {
                //arrange
                when(member.getEffectiveName()).thenReturn(MEMBER_NAME_DEFAULT);
                when(messageRemove.getContentRaw()).thenReturn(RegistrationService.REMOVE_COMMAND);
                //act
                triggerMessageReceivedEvent(messageRemove, member, MEMBER_NAME_DEFAULT);
                //assert
                assertThat(testee.getAllPlayers().size()).isEqualTo(0);
                verify(jdaService).sendMessage(RegistrationService.WAS_NOT_REGISTERED);
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