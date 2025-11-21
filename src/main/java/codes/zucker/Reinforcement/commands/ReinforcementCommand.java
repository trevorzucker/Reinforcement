package codes.zucker.reinforcement.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import codes.zucker.reinforcement.ReinforcementPlugin;

@CommandInfo(command = "reinforcement", description = "Reinforcement admin management", permission = "Reinforced.admin")
public class ReinforcementCommand implements PluginCommand {

  @Override
  public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 0) {
      args = new String[] {""};
    }

    switch(args[0]) {
      case "reload":
        Bukkit.getPluginManager().disablePlugin(JavaPlugin.getPlugin(ReinforcementPlugin.class));
        Bukkit.getPluginManager().enablePlugin(JavaPlugin.getPlugin(ReinforcementPlugin.class));
        sender.sendMessage("Reloaded Reinforcement!");
        break;
      default:
        sender.sendMessage("/reinforcement [reload]");
    }
    return true;
  }

}
