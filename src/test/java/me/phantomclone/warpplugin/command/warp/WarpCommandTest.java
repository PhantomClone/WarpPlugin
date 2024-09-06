package me.phantomclone.warpplugin.command.warp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.command.PhoenixCommand;
import me.phantomclone.warpplugin.command.SubCommand;
import me.phantomclone.warpplugin.service.WarpService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class WarpCommandTest {

    @Mock
    WarpPlugin plugin;

    @Mock
    WarpService warpService;

    @Mock
    WarpCache warpCache;

    @Mock
    PluginCommand pluginCommand;

    @InjectMocks
    WarpCommand warpCommand;

    @Captor
    ArgumentCaptor<CommandExecutor> executorCaptor;

    @Test
    void testRegisterCommand_Success() {
        when(plugin.getCommand("warp")).thenReturn(pluginCommand);

        warpCommand.registerCommand();

        verify(pluginCommand).setExecutor(executorCaptor.capture());
        CommandExecutor executor = executorCaptor.getValue();
        assertEquals(warpCommand, executor);
    }

    @Test
    void testRegisterCommand_CommandNotFound() {
        when(plugin.getCommand("warp")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, warpCommand::registerCommand);
        assertEquals("Could not find 'warp' command. Check plugin.yml!", exception.getMessage());
    }

    @Test
    void testSenderClass() {
        assertEquals(Player.class, warpCommand.senderClass());
    }

    @Test
    void testSubCommandsRegistration() {
        when(plugin.getCommand("warp")).thenReturn(pluginCommand);

        warpCommand.registerCommand();

        List<SubCommand<Player>> subCommands = getSubCommandsViaReflection();

        assertNotNull(subCommands);
        assertEquals(4, subCommands.size());
        assertTrue(subCommands.get(0) instanceof CreateWarpSubCommand);
        assertTrue(subCommands.get(1) instanceof DeleteWarpSubCommand);
        assertTrue(subCommands.get(2) instanceof TeleportWarpSubCommand);
        assertTrue(subCommands.get(3) instanceof ListWarpSubCommand);
    }

    @Test
    void testNotFound() {
        Player mockPlayer = mock(Player.class);

        assertFalse(warpCommand.notFound(mockPlayer));
    }

    @SuppressWarnings("unchecked")
    List<SubCommand<Player>> getSubCommandsViaReflection() {
        try {
            var field = PhoenixCommand.class.getDeclaredField("subCommandList");
            field.setAccessible(true);
            return (List<SubCommand<Player>>) field.get(warpCommand);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Fehler beim Zugriff auf subCommandList via Reflection: " + e.getMessage());
            return List.of();
        }
    }
}
