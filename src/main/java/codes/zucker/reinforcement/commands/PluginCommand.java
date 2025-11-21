package codes.zucker.reinforcement.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface PluginCommand {
  boolean execute(CommandSender sender, Command cmd, String label, String[] args);
}
