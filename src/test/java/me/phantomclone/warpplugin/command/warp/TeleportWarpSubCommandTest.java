package me.phantomclone.warpplugin.command.warp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeleportWarpSubCommandTest {

    @Mock
    WarpPlugin plugin;

    @Mock
    Server server;

    @Mock
    BukkitScheduler scheduler;

    @Mock
    WarpService warpService;

    @Mock
    WarpCache warpCache;

    @Mock
    Player player;

    @InjectMocks
    TeleportWarpSubCommand teleportWarpSubCommand;

    @Captor
    ArgumentCaptor<Consumer<Optional<Warp>>> consumerCaptor;

    @Test
    void testValidate_CorrectArguments() {
        String[] args = {"teleport", "warpName"};

        assertTrue(teleportWarpSubCommand.validate(player, args));
    }

    @Test
    void testValidate_IncorrectArguments() {
        String[] args = {"wrongCommand", "warpName"};

        assertFalse(teleportWarpSubCommand.validate(player, args));
    }

    @Test
    void testExecute_WarpNotFound() {
        String[] args = {"teleport", "nonExistentWarp"};
        UUID playerUuid = UUID.randomUUID();
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(player.getUniqueId()).thenReturn(playerUuid);
        CompletableFuture<Optional<Warp>> mockCompletableFuture = createMockCompletableFuture();
        when(warpService.getWarp(playerUuid, "nonExistentWarp"))
                .thenReturn(mockCompletableFuture);

        teleportWarpSubCommand.execute(player, args);

        verify(mockCompletableFuture).thenAcceptAsync(consumerCaptor.capture(), eq(null));
        consumerCaptor.getValue().accept(Optional.empty());
        verify(player).sendMessage(containsInMessage(message -> message.contains("nicht gefunden")));
    }

    @Test
    void testExecute_WarpFound() {
        String[] args = {"teleport", "warpName"};
        UUID playerUuid = UUID.randomUUID();
        Location warpLocation = mock(Location.class);
        Warp warp = new Warp().setPlayerUuid(playerUuid).setWarpName("warpName").setLocation(warpLocation);

        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(player.getUniqueId()).thenReturn(playerUuid);
        CompletableFuture<Optional<Warp>> mockCompletableFuture = createMockCompletableFuture();
        when(warpService.getWarp(playerUuid, "warpName"))
                .thenReturn(mockCompletableFuture);

        teleportWarpSubCommand.execute(player, args);

        verify(mockCompletableFuture).thenAcceptAsync(consumerCaptor.capture(), eq(null));
        consumerCaptor.getValue().accept(Optional.of(warp));
        verify(player).sendMessage(containsInMessage(message -> message.contains("gestartet...")));
        verify(player).teleport(warpLocation);
        verify(player).sendMessage(containsInMessage(message -> message.contains("Teleportation abgeschlossen.")));
    }

    @Test
    void testOnTabComplete_CorrectArgumentOne() {
        String[] args = {"tele"};
        Command command = mock(Command.class);
        List<String> completions = teleportWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(1, completions.size());
        assertTrue(completions.contains("teleport"));
    }

    @Test
    void testOnTabComplete_CorrectArgumentTwo() {
        String[] args = {"teleport", ""};
        Command command = mock(Command.class);
        List<String> warpNames = List.of("warp1", "warp2");
        UUID playerUuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUuid);
        when(warpCache.getWarpNameCache(playerUuid)).thenReturn(warpNames);

        List<String> completions = teleportWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(2, completions.size());
        assertTrue(completions.contains("warp1"));
        assertTrue(completions.contains("warp2"));
    }

    @Test
    void testOnTabComplete_IncorrectArgument() {
        String[] args = {};
        Command command = mock(Command.class);
        List<String> completions = teleportWarpSubCommand.onTabComplete(player, command, "label", args);

        assertEquals(0, completions.size());
    }

    @SuppressWarnings("unchecked")
    static CompletableFuture<Optional<Warp>> createMockCompletableFuture() {
        return (CompletableFuture<Optional<Warp>>) mock(CompletableFuture.class);
    }

    static Component containsInMessage(Predicate<String> prediction) {
        return ArgumentMatchers.argThat(component ->
                prediction.test(PlainTextComponentSerializer.plainText().serialize(component)));
    }
}
