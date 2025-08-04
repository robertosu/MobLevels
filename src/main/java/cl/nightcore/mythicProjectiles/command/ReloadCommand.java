package cl.nightcore.mythicProjectiles.command;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    MythicProjectiles plugin;

    public ReloadCommand(MythicProjectiles plugin) {
        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String [] strings) {

        switch (strings[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadPlugin();
                return true;
            }
        }
        return false;
    }
}
