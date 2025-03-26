package codes.zucker.reinforcement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import codes.zucker.reinforcement.util.LangYaml;
import codes.zucker.reinforcement.util.Utils;

public class Commands {
    
    protected static List<Player> rvToggle = new ArrayList<>();
    protected static List<Player> reToggle = new ArrayList<>();
    
    public static boolean rvCommand(CommandSender sender, Command cmd, String label, String[] args) {
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

    private Commands() {
        
    }

    public static boolean reCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
}