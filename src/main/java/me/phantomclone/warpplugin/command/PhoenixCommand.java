package me.phantomclone.warpplugin.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PhoenixCommand<S extends CommandSender> implements CommandExecutor, TabCompleter {

    private final List<SubCommand<S>> subCommandList = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!senderClass().isAssignableFrom(sender.getClass())) {
            return wrongSenderType(sender);
        }

        S castSender = senderClass().cast(sender);

        return subCommandList.stream()
                .filter(subCommand -> subCommand.hasPermission(castSender))
                .filter(subCommand -> subCommand.validate(castSender, args))
                .findFirst()
                .map(subCommand -> executeSubCommand(subCommand, castSender, args))
                .orElseGet(() -> notFound(castSender));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!senderClass().isAssignableFrom(sender.getClass())) {
            return List.of();
        }

        S castSender = senderClass().cast(sender);

        return subCommandList.stream()
                .flatMap(subCommand -> subCommand.onTabComplete(castSender, command, label, args).stream())
                .toList();
    }

    private boolean executeSubCommand(SubCommand<S> subCommand, S castSender, String[] args) {
        subCommand.execute(castSender, args);
        return true;
    }

    protected abstract Class<S> senderClass();

    protected boolean notFound(S sender) {
        return true;
    }

    protected boolean wrongSenderType(CommandSender commandSender) {
        commandSender.sendMessage(Component.text(String.format("Du bist kein %s.", senderClass().getSimpleName())));
        return true;
    }

    @SafeVarargs
    protected final void registerSubCommand(SubCommand<S>... subCommand) {
        subCommandList.addAll(Arrays.asList(subCommand));
    }
}
