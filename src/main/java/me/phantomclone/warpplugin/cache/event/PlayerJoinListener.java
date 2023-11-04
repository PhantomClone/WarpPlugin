package me.phantomclone.warpplugin.cache.event;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.injection.PostConstructor;
import me.phantomclone.warpplugin.service.WarpService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Bean
@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final WarpPlugin warpPlugin;
    private final WarpService warpService;

    @PostConstructor
    public void postConstructor() {
        warpPlugin.getServer().getPluginManager().registerEvents(this, warpPlugin);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        warpService.getWarpsOf(playerJoinEvent.getPlayer().getUniqueId());
    }

}
