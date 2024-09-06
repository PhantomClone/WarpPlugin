package me.phantomclone.warpplugin.command.warp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateWarpSubCommandTest {

    @Mock
    WarpService warpService;

    @Mock
    Player player;

    @Mock
    WarpPlugin plugin;

    @Mock
    Logger logger;

    @InjectMocks
    CreateWarpSubCommand createWarpSubCommand;

    @Test
    void testValidate_CorrectArguments() {
        String[] args = {"create", "warpName"};

        assertTrue(createWarpSubCommand.validate(player, args));
    }

    @Test
    void testWarpService_SaveError() {
        String[] args = {"create", "warpName"};

        assertTrue(createWarpSubCommand.validate(player, args));
    }

    @Test
    void testValidate_IncorrectArguments() {
        String[] args = {"wrongCommand", "warpName"};

        assertFalse(createWarpSubCommand.validate(player, args));
    }

    @Test
    void testExecute_InvalidWarpNameTooShort() {
        String[] args = {"create", "war"};

        createWarpSubCommand.execute(player, args);

        verify(player).sendMessage(compareMessage("Dein Warp war muss mindestens 4 Zeichen lang sein!"));
    }

    @Test
    void testExecute_InvalidWarpNameTooLong() {
        String[] args = {"create", "warpNameIsTooLong"};

        createWarpSubCommand.execute(player, args);

        verify(player).sendMessage(compareMessage("Dein Warp warpNameIsTooLong darf nicht länger als 10 Zeichen lang sein!"));
    }

    @Test
    void testExecute_InvalidWarpNameWithSpecialCharacters() {
        String[] args = {"create", "warp!name"};

        createWarpSubCommand.execute(player, args);

        verify(player).sendMessage(compareMessage("Dein Warp warp!name darf keine Sonderzeichen enthalten! (!)"));
    }

    @Test
    void testExecute_ValidWarpName() {
        String[] args = {"create", "warpName"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(player.getLocation()).thenReturn(mock(Location.class));
        when(warpService.createWarp(any(Warp.class))).thenReturn(CompletableFuture.completedFuture(true));

        createWarpSubCommand.execute(player, args);
        verify(player).sendMessage(compareMessage("Dein Warp wird erstellt..."));
        verify(warpService).createWarp(any(Warp.class));
    }

    @Test
    void testExecute_WarpServiceErrorWarpName() {
        String[] args = {"create", "warpName"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getName()).thenReturn("Max");
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(player.getLocation()).thenReturn(mock(Location.class));
        Throwable shouldThrow = new Throwable("SHOULD THROW");
        when(warpService.createWarp(any(Warp.class))).thenReturn(CompletableFuture.failedFuture(shouldThrow));
        when(plugin.getLogger()).thenReturn(logger);

        createWarpSubCommand.execute(player, args);
        verify(logger).log(eq(Level.SEVERE), eq("Warp warpName could not be stored! (PlayerName=Max)"), eq(shouldThrow));
        verify(player).sendMessage(compareMessage("Dein Warp warpName konnte nicht erstellt werden!"));
        verify(warpService).createWarp(any(Warp.class));
    }

    @Test
    void testExecute_WarpServiceFalseWarpName() {
        String[] args = {"create", "warpName"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(player.getLocation()).thenReturn(mock(Location.class));
        when(warpService.createWarp(any(Warp.class))).thenReturn(CompletableFuture.completedFuture(false));

        createWarpSubCommand.execute(player, args);
        verify(player).sendMessage(compareMessage("Dein Warp warpName konnte nicht erstellt werden!"));
        verify(warpService).createWarp(any(Warp.class));
    }

    @Test
    void testOnTabComplete_CorrectArgumentOne() {
        String[] args = {"cre"};
        Command command = mock(Command.class);
        List<String> completions = createWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(1, completions.size());
        assertTrue(completions.contains("create"));
    }

    @Test
    void testOnTabComplete_CorrectArgumentTwo() {
        String[] args = {"create"};
        Command command = mock(Command.class);
        List<String> completions = createWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(1, completions.size());
        assertTrue(completions.contains("create"));
    }

    @Test
    void testOnTabComplete_IncorrectArgument() {
        String[] args = {};
        Command command = mock(Command.class);
        List<String> completions = createWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(0, completions.size());
    }

    static Component compareMessage(String compareText) {
        return ArgumentMatchers.argThat(component ->
                PlainTextComponentSerializer.plainText().serialize(component)
                        .equals(compareText));
    }

}
