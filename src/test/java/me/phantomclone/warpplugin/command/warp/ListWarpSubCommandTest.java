package me.phantomclone.warpplugin.command.warp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

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
class ListWarpSubCommandTest {

    @Mock
    WarpService warpService;

    @Mock
    Player player;

    @InjectMocks
    ListWarpSubCommand listWarpSubCommand;

    @Test
    void testValidate_CorrectArguments() {
        String[] args = {"list"};

        assertTrue(listWarpSubCommand.validate(player, args));
    }

    @Test
    void testValidate_IncorrectArguments() {
        String[] args = {"wrongCommand"};

        assertFalse(listWarpSubCommand.validate(player, args));
    }

    @Test
    void testExecute_NoWarps() {
        String[] args = {"list"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(warpService.getWarpsOf(playerUuid)).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        listWarpSubCommand.execute(player, args);

        verify(player).sendMessage(compareMessage("Du hast keine Warps."));
    }

    @Test
    void testExecute_WithWarps() {
        String[] args = {"list"};
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);

        List<Warp> warpList = List.of(
                createWarp(), createWarp()
        );

        when(warpService.getWarpsOf(playerUuid)).thenReturn(CompletableFuture.completedFuture(warpList));

        listWarpSubCommand.execute(player, args);

        verify(player).sendMessage(containsInMessage(message -> message.startsWith("Dein Warps:\n")
                && message.split("\n").length == 3));
    }

    @Test
    void testOnTabComplete_CorrectArgument() {
        String[] args = {"li"};
        Command command = mock(Command.class);
        List<String> completions = listWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(1, completions.size());
        assertTrue(completions.contains("list"));
    }

    @Test
    void testOnTabComplete_IncorrectArgument() {
        String[] args = {};
        Command command = mock(Command.class);
        List<String> completions = listWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(0, completions.size());
    }

    static Component compareMessage(String compareText) {
        return ArgumentMatchers.argThat(component ->
                PlainTextComponentSerializer.plainText().serialize(component)
                        .equals(compareText));
    }

    static Component containsInMessage(Predicate<String> prediction) {
        return ArgumentMatchers.argThat(component ->
                        prediction.test(PlainTextComponentSerializer.plainText().serialize(component)));
    }

    Warp createWarp() {
        return createWarp(UUID.randomUUID(), UUID.randomUUID().toString(), mock(Location.class));
    }

    Warp createWarp(UUID uuid, String warpName, Location location) {
        return new Warp().setPlayerUuid(uuid)
                .setWarpName(warpName)
                .setLocation(location);
    }
}