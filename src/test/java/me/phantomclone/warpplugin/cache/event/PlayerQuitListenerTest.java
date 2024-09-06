package me.phantomclone.warpplugin.cache.event;

import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.cache.WarpCache;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerQuitListenerTest {

    @Mock
    WarpPlugin warpPlugin;

    @Mock
    Server server;

    @Mock
    PluginManager pluginManager;

    @Mock
    WarpCache warpCache;

    @Mock
    PlayerQuitEvent playerQuitEvent;

    @Mock
    Player player;

    @InjectMocks
    PlayerQuitListener playerQuitListener;

    @Test
    void testPostConstructor_ShouldRegisterListener() {
        when(warpPlugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        playerQuitListener.postConstructor();

        verify(pluginManager).registerEvents(playerQuitListener, warpPlugin);
    }

    @Test
    void testOnPlayerJoinEvent_ShouldCallGetWarpsOf() {
        UUID playerUUID = UUID.randomUUID();
        when(playerQuitEvent.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerUUID);

        playerQuitListener.onPlayerQuitEvent(playerQuitEvent);

        verify(warpCache).clear(playerUUID);
    }
}
