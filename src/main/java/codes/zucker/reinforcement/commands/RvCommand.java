package codes.zucker.reinforcement.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import codes.zucker.reinforcement.util.LangYaml;
import codes.zucker.reinforcement.util.Utils;

@CommandInfo(command = "rv", description = "Toggles viewing of reinforced blocks", permission = "Reinforced.rv")
public class RvCommand implements PluginCommand {

  protected static List<Player> rvToggle = new ArrayList<>();

  @Override
  public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {

    if (sender instanceof ConsoleCommandSender) {
      return true;
    }

    Player player = (Player)sender;

    if (rvToggle.contains(player)) {
      rvToggle.remove(player);
      Utils.sendMessage(player, LangYaml.getString("reinforcement_visibility_off"));
    }
    else {
      rvToggle.add(player);
      Utils.sendMessage(player, LangYaml.getString("reinforcement_visibility_on"));
    }
    return true;
  }

  public static List<Player> getToggledPlayers() {
    return rvToggle;
  }
}
