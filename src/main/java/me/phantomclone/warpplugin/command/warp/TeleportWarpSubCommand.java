package me.phantomclone.warpplugin.command.warp;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.command.SubCommand;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

@RequiredArgsConstructor
public class TeleportWarpSubCommand implements SubCommand<Player> {

    private final WarpPlugin plugin;
    private final WarpService warpService;
    private final WarpCache warpCache;

    @Override
    public boolean validate(Player sender, String[] args) {
        return args.length == 2 && args[0].equalsIgnoreCase("teleport");
    }

    @Override
    public void execute(Player player, String[] args) {
        String warpName = args[1];

        player.sendMessage(startTeleportComponent(warpName));

        warpService.getWarp(player.getUniqueId(), warpName)
                .thenAcceptAsync(optionalWarp -> optionalWarp
                                .ifPresentOrElse(warp -> teleportTeleportToWarp(player, warp),
                                        () -> teleportWarpNotFound(player, warpName)),
                        plugin.getServer().getScheduler().getMainThreadExecutor(plugin));
    }

    @Override
    public List<String> onTabComplete(Player sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("teleport");
        } else if (args.length == 2) {
            return List.copyOf(warpCache.getWarpNameCache(sender.getUniqueId()));
        }

        return List.of();
    }

    private void teleportTeleportToWarp(Player player, Warp warp) {
        player.teleport(warp.getLocation());
        player.sendMessage(miniMessage().deserialize("<gray>Teleportation abgeschlossen."));
    }

    private void teleportWarpNotFound(Player player, String warpName) {
        player.sendMessage(miniMessage().deserialize("<red><warpname> <gray>nicht gefunden.",
                parsed("warpname", warpName)));
    }

    private static Component startTeleportComponent(String warpName) {
        return miniMessage().deserialize("<gray>Teleportation nach <green><warpname> <gray>gestartet...",
                parsed("warpname", warpName));
    }
}
