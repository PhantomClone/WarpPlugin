package me.phantomclone.warpplugin.command.warp;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.command.SubCommand;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.service.WarpService;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed;

@RequiredArgsConstructor
public class CreateWarpSubCommand implements SubCommand<Player> {

    private final WarpService warpService;
    private static final Pattern PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");

    @Override
    public boolean validate(Player sender, String[] args) {
        return args.length == 2 && args[0].equalsIgnoreCase("create");
    }

    @Override
    public void execute(Player player, String[] args) {
        String warpName = args[1];

        validateWarpName(warpName)
                .ifPresentOrElse(player::sendMessage, () -> processCreateWarpCommand(player, warpName));
    }

    private void processCreateWarpCommand(Player player, String warpName) {
        Warp warp = new Warp().setPlayerUuid(player.getUniqueId())
                .setWarpName(warpName)
                .setLocation(player.getLocation());

        player.sendMessage(miniMessage().deserialize("<gray>Dein Warp wird erstellt..."));

        createWarp(warp, player);
    }


    @Override
    public List<String> onTabComplete(Player sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("create");
        }

        return List.of();
    }

    private void createWarp(Warp warp, Player player) {
        warpService.createWarp(warp).thenAccept(result -> sendResultOfWarpCreate(result, player, warp.getWarpName()));
    }

    private void sendResultOfWarpCreate(boolean result, Player player, String warpName) {
        player.sendMessage(result ? successfullyCreateWarp(warpName) : cannotCreateWarp(warpName));
    }

    private static Component successfullyCreateWarp(String warpName) {
        return miniMessage()
                .deserialize("<gray>Dein Warp <green><warpname> <gray>wurde erfolgreich erstellt.",
                        parsed("warpname", warpName));
    }

    private static Component cannotCreateWarp(String warpName) {
        return miniMessage()
                .deserialize("<gray>Dein Warp <red><warpname> <gray>konnte nicht erstellt werden!",
                        parsed("warpname", warpName));
    }

    private Optional<Component> validateWarpName(String warpName) {
        if (warpName.length() < 4) {
            return Optional.of(miniMessage().deserialize("<gray>Dein Warp <red><warpname> <gray>muss mindestens 4 Zeichen lang sein!",
                    parsed("warpname", warpName)));
        } else if (warpName.length() > 10) {
            return Optional.of(miniMessage().deserialize("<gray>Dein Warp <red><warpname> <gray>darf nicht länger als 10 Zeichen lang sein!",
                    parsed("warpname", warpName)));
        }

        Matcher matcher = PATTERN.matcher(warpName);
        if (matcher.find()) {
            return Optional.of(miniMessage().deserialize("<gray>Dein Warp <warpname> darf keine Sonderzeichen enthalten! (<sonderzeichen>) ",
                    parsed("warpname", warpName), parsed("sonderzeichen", matcher.group())));
        }

        return Optional.empty();
    }
}
