package me.phantomclone.warpplugin.command.warp;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.command.SubCommand;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

@RequiredArgsConstructor
public class DeleteWarpSubCommand implements SubCommand<Player> {

    private final WarpService warpService;
    private final WarpCache warpCache;

    @Override
    public boolean validate(Player sender, String[] args) {
        return args.length == 2 && args[0].equalsIgnoreCase("delete");
    }

    @Override
    public void execute(Player player, String[] args) {
        String warpName = args[1];

        warpService.deleteWarp(player.getUniqueId(), warpName)
                .thenAccept(result -> sendResultOfWarpDelete(result, player, warpName));
    }

    @Override
    public  List<String> onTabComplete(Player sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("delete");
        } else if (args.length == 2) {
            return List.copyOf(warpCache.getWarpNameCache(sender.getUniqueId()));
        }

        return List.of();
    }

    private void sendResultOfWarpDelete(boolean result, Player player, String warpName) {
        player.sendMessage(result ? successfullyDeleteWarp(warpName) : cannotDeleteWarp(warpName));
    }

    private static Component successfullyDeleteWarp(String warpName) {
        return miniMessage()
                .deserialize("<gray>Dein Warp <green><warpname> <gray>wurde erfolgreich gelöscht.",
                        parsed("warpname", warpName));
    }

    private static Component cannotDeleteWarp(String warpName) {
        return miniMessage()
                .deserialize("<gray>Dein Warp <red><warpname> <gray>konnte nicht gelöscht.",
                        parsed("warpname", warpName));
    }
}
