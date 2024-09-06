package me.phantomclone.warpplugin.command.warp;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.command.SubCommand;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

@RequiredArgsConstructor
public class ListWarpSubCommand implements SubCommand<Player> {

    private static final String SINGLE_WARP_TEXT = "<newline>  <gray><hover:show_text:'<gray>Teleportiere dich nach <green><warpname>'><click:run_command:'/warp teleport <warpname>'><warpname>";

    private final WarpService warpService;

    @Override
    public boolean validate(Player sender, String[] args) {
        return args.length == 1 && args[0].equalsIgnoreCase("list");
    }

    @Override
    public void execute(Player player, String[] args) {
        warpService.getWarpsOf(player.getUniqueId())
                .thenAccept(warpList -> sendPlayerWarps(player, warpList));
    }

    public  List<String> onTabComplete(Player sender, Command command, String label, String[] args) {
        return args.length == 1 && "list".startsWith(args[0]) ? List.of("list") : List.of();
    }

    private void sendPlayerWarps(Player player, List<Warp> warpList) {
        if (warpList.isEmpty()) {
            player.sendMessage(miniMessage().deserialize("<gray>Du hast keine Warps."));
        } else {
            player.sendMessage(warpListComponent(warpList));
        }
    }

    @NotNull
    private static Component warpListComponent(List<Warp> warpList) {
        Component message = miniMessage().deserialize("<gray>Dein Warps:");

        for (Component component : getMessage(warpList)) {
            message = message.append(component);
        }

        return message;
    }

    private static List<Component> getMessage(List<Warp> warpList) {
        return warpList.stream()
                .map(Warp::getWarpName)
                .map(warpName -> miniMessage().deserialize(SINGLE_WARP_TEXT, parsed("warpname", warpName)))
                .toList();
    }
}
