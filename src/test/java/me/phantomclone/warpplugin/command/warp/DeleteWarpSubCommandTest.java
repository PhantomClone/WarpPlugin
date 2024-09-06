package me.phantomclone.warpplugin.command.warp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteWarpSubCommandTest {

    @Mock
    WarpService warpService;

    @Mock
    WarpCache warpCache;

    @Mock
    Player player;

    @InjectMocks
    DeleteWarpSubCommand deleteWarpSubCommand;

    @Test
    void testValidate_CorrectArguments() {
        String[] args = {"delete", "warpName"};

        assertTrue(deleteWarpSubCommand.validate(player, args));
    }

    @Test
    void testValidate_IncorrectArguments() {
        String[] args = {"wrongCommand", "warpName"};

        assertFalse(deleteWarpSubCommand.validate(player, args));
    }

    @Test
    void testExecute_DeleteWarpSuccessfully() {
        String[] args = {"delete", "warpName"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(warpService.deleteWarp(playerUuid, "warpName")).thenReturn(CompletableFuture.completedFuture(true));

        deleteWarpSubCommand.execute(player, args);

        verify(player).sendMessage(compareMessage("Dein Warp warpName wurde erfolgreich gelöscht."));
        verify(warpService).deleteWarp(playerUuid, "warpName");
    }

    @Test
    void testExecute_DeleteWarpUnsuccessfully() {
        String[] args = {"delete", "warpName"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(warpService.deleteWarp(playerUuid, "warpName")).thenReturn(CompletableFuture.completedFuture(false));

        deleteWarpSubCommand.execute(player, args);

        verify(player).sendMessage(compareMessage("Dein Warp warpName konnte nicht gelöscht."));
        verify(warpService).deleteWarp(playerUuid, "warpName");
    }

    @Test
    void testOnTabComplete_CorrectArgumentOne() {
        String[] args = {"del"};
        Command command = mock(Command.class);
        List<String> completions = deleteWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(1, completions.size());
        assertTrue(completions.contains("delete"));
    }

    @Test
    void testOnTabComplete_CorrectArgumentTwo() {
        String[] args = {"delete", ""};
        Command command = mock(Command.class);
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(warpCache.getWarpNameCache(playerUuid)).thenReturn(List.of("warp1", "warp2"));
        List<String> completions = deleteWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(2, completions.size());
        assertTrue(completions.contains("warp1"));
        assertTrue(completions.contains("warp2"));
    }

    @Test
    void testOnTabComplete_CorrectArgumentThree() {
        String[] args = {"delete", "a"};
        Command command = mock(Command.class);
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(warpCache.getWarpNameCache(playerUuid)).thenReturn(List.of("a_warp", "b_warp"));
        List<String> completions = deleteWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(1, completions.size());
        assertTrue(completions.contains("a_warp"));
    }

    @Test
    void testOnTabComplete_IncorrectArgument() {
        String[] args = {};
        Command command = mock(Command.class);
        List<String> completions = deleteWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(0, completions.size());
    }

    static Component compareMessage(String compareText) {
        return ArgumentMatchers.argThat(component ->
                PlainTextComponentSerializer.plainText().serialize(component)
                        .equals(compareText));
    }
}
