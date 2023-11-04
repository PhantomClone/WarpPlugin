package me.phantomclone.warpplugin.command.warp;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.command.PhoenixCommand;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.injection.PostConstructor;
import me.phantomclone.warpplugin.service.WarpService;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

@Bean
@RequiredArgsConstructor
public class WarpCommand extends PhoenixCommand<Player> {

    private final WarpPlugin plugin;
    private final WarpService warpService;
    private final WarpCache warpCache;

    @PostConstructor
    public void registerCommand() {
        PluginCommand command = plugin.getCommand("warp");
        if (command == null) {
            throw new RuntimeException("Could not find 'warp' command. Check plugin.yml!");
        }

        registerSubCommand(
              new CreateWarpSubCommand(warpService),
              new DeleteWarpSubCommand(warpService, warpCache),
              new TeleportWarpSubCommand(plugin, warpService, warpCache),
              new ListWarpSubCommand(warpService)
        );

        command.setExecutor(this);
    }

    @Override
    protected Class<Player> senderClass() {
        return Player.class;
    }

    @Override
    protected boolean notFound(Player sender) {
        return false;
    }
}
