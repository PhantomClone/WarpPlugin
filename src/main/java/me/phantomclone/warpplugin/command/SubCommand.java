package me.phantomclone.warpplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand<S extends CommandSender> {

    default boolean hasPermission(S sender) {
        return true;
    }

    boolean validate(S sender, String[] args);

    void execute(S sender, String[] args);

    default List<String> onTabComplete(S sender, Command command, String label, String[] args) {
        return List.of();
    }


}
