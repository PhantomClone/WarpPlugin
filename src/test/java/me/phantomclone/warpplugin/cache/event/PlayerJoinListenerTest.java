package me.phantomclone.warpplugin.cache.event;

import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.service.WarpService;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerJoinListenerTest {

    @Mock
    WarpPlugin warpPlugin;

    @Mock
    Server server;

    @Mock
    PluginManager pluginManager;

    @Mock
    WarpService warpService;

    @Mock
    PlayerJoinEvent playerJoinEvent;

    @Mock
    Player player;

    @InjectMocks
    PlayerJoinListener playerJoinListener;

    @Test
    void testPostConstructor_ShouldRegisterListener() {
        when(warpPlugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        playerJoinListener.postConstructor();

        verify(pluginManager).registerEvents(playerJoinListener, warpPlugin);
    }

    @Test
    void testOnPlayerJoinEvent_ShouldCallGetWarpsOf() {
        UUID playerUUID = UUID.randomUUID();
        when(playerJoinEvent.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerUUID);

        playerJoinListener.onPlayerJoinEvent(playerJoinEvent);

        verify(warpService).getWarpsOf(playerUUID);
    }
}
