package me.phantomclone.warpplugin.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

class PhoenixCommandTest {

    @Mock
    Player mockPlayer;

    @Mock
    Command mockCommand;

    @Mock
    SubCommand<Player> mockSubCommand1;

    @Mock
    SubCommand<Player> mockSubCommand2;

    TestPhoenixCommand testPhoenixCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPhoenixCommand = new TestPhoenixCommand();

        testPhoenixCommand.registerSubCommand(mockSubCommand1, mockSubCommand2);
    }

    @Test
    void testOnCommand_CorrectSenderAndValidSubCommand() {
        String[] args = {"arg1", "arg2"};
        when(mockSubCommand1.hasPermission(mockPlayer)).thenReturn(true);
        when(mockSubCommand1.validate(mockPlayer, args)).thenReturn(true);

        boolean result = testPhoenixCommand.onCommand(mockPlayer, mockCommand, "label", args);

        assertTrue(result);
        verify(mockSubCommand1).execute(mockPlayer, args);
        verify(mockSubCommand2, never()).execute(any(), any());
    }

    @Test
    void testOnCommand_CorrectSenderButNoValidSubCommand() {
        String[] args = {"invalidCommand"};
        when(mockSubCommand1.hasPermission(mockPlayer)).thenReturn(true);
        when(mockSubCommand1.validate(mockPlayer, args)).thenReturn(false);
        when(mockSubCommand2.hasPermission(mockPlayer)).thenReturn(true);
        when(mockSubCommand2.validate(mockPlayer, args)).thenReturn(false);

        boolean result = testPhoenixCommand.onCommand(mockPlayer, mockCommand, "label", args);

        assertTrue(result);
        verify(mockSubCommand1, never()).execute(any(), any());
        verify(mockSubCommand2, never()).execute(any(), any());
    }

    @Test
    void testOnCommand_WrongSenderType() {
        CommandSender sender = mock(CommandSender.class);
        String[] args = {"any"};

        boolean result = testPhoenixCommand.onCommand(sender, mockCommand, "label", args);

        assertTrue(result);
        verify(sender).sendMessage(compareMessage("Du bist kein Player."));
    }

    @Test
    void testOnTabComplete_CorrectSender() {
        String[] args = {"ar"};
        when(mockSubCommand1.onTabComplete(mockPlayer, mockCommand, "label", args)).thenReturn(List.of("arg1"));
        when(mockSubCommand2.onTabComplete(mockPlayer, mockCommand, "label", args)).thenReturn(List.of("arg2"));

        List<String> completions = testPhoenixCommand.onTabComplete(mockPlayer, mockCommand, "label", args);

        assertNotNull(completions);
        assertEquals(2, completions.size());
        assertTrue(completions.contains("arg1"));
        assertTrue(completions.contains("arg2"));
    }

    @Test
    void testOnTabComplete_WrongSenderType() {
        CommandSender sender = mock(CommandSender.class);
        String[] args = {"any"};

        List<String> completions = testPhoenixCommand.onTabComplete(sender, mockCommand, "label", args);

        assertNotNull(completions);
        assertTrue(completions.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRegisterSubCommand() {
        SubCommand<Player> mockSubCommand3 = (SubCommand<Player>) mock(SubCommand.class);
        testPhoenixCommand.registerSubCommand(mockSubCommand3);

        List<SubCommand<Player>> subCommandsViaReflection = getSubCommandsViaReflection();

        assertEquals(3, subCommandsViaReflection.size());
        assertTrue(subCommandsViaReflection.contains(mockSubCommand3));
    }
    @Test
    void testNotFoundMethod() {
        String[] args = {"invalidCommand"};
        when(mockSubCommand1.hasPermission(mockPlayer)).thenReturn(true);
        when(mockSubCommand1.validate(mockPlayer, args)).thenReturn(false);
        when(mockSubCommand2.hasPermission(mockPlayer)).thenReturn(true);
        when(mockSubCommand2.validate(mockPlayer, args)).thenReturn(false);

        boolean result = testPhoenixCommand.onCommand(mockPlayer, mockCommand, "label", args);

        assertTrue(result);
        verify(mockPlayer, never()).sendMessage(any(Component.class));
    }

    static Component compareMessage(String compareText) {
        return ArgumentMatchers.argThat(component ->
                PlainTextComponentSerializer.plainText().serialize(component)
                        .equals(compareText));
    }

    @SuppressWarnings("unchecked")
    List<SubCommand<Player>> getSubCommandsViaReflection() {
        try {
            var field = PhoenixCommand.class.getDeclaredField("subCommandList");
            field.setAccessible(true);
            return (List<SubCommand<Player>>) field.get(testPhoenixCommand);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Fehler beim Zugriff auf subCommandList via Reflection: " + e.getMessage());
            return List.of();
        }
    }

    static class TestPhoenixCommand extends PhoenixCommand<Player> {

        @Override
        protected Class<Player> senderClass() {
            return Player.class;
        }

    }
}
