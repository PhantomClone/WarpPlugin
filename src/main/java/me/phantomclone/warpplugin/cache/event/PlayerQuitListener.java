package me.phantomclone.warpplugin.cache.event;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.injection.PostConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Bean
@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {
    
    private final WarpPlugin warpPlugin;
    private final WarpCache warpCache;
    
    @PostConstructor
    public void postConstructor() {
        warpPlugin.getServer().getPluginManager().registerEvents(this, warpPlugin);
    }
    
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent playerQuitEvent) {
        warpCache.clear(playerQuitEvent.getPlayer().getUniqueId());
    }
    
}
