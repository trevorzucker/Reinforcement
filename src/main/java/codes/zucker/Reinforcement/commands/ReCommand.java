package codes.zucker.reinforcement.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import codes.zucker.reinforcement.util.LangYaml;
import codes.zucker.reinforcement.util.Utils;

@CommandInfo(command = "re", description = "Toggles reinforcement mode", permission = "Reinforced.re")
public class ReCommand implements PluginCommand {

  protected static List<Player> reToggle = new ArrayList<>();

  @Override
  public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof ConsoleCommandSender) {
      return true;
    }

    Player player = (Player)sender;

    if (reToggle.contains(player)) {
      reToggle.remove(player);
      Utils.sendMessage(player, LangYaml.getString("reinforcement_mode_off"));
    }
    else {
      reToggle.add(player);
      Utils.sendMessage(player, LangYaml.getString("reinforcement_mode_on"));
    }
    return true;
  }

  public static List<Player> getToggledPlayers() {
    return reToggle;
  }
}
