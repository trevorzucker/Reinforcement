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
    public static boolean rvCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;

        if (rvToggle.contains(p)) {
            rvToggle.remove(p);
            Utils.sendMessage(p, LangYaml.getString("reinforcement_visibility_off"));
        }
        else {
            rvToggle.add(p);
            Utils.sendMessage(p, LangYaml.getString("reinforcement_visibility_on"));
        }
        return true;
    }

    private Commands() {
        
    }

    protected static List<Player> reToggle = new ArrayList<>();
    public static boolean reCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;

        if (reToggle.contains(p)) {
            reToggle.remove(p);
            Utils.sendMessage(p, LangYaml.getString("reinforcement_mode_off"));
        }
        else {
            reToggle.add(p);
            Utils.sendMessage(p, LangYaml.getString("reinforcement_mode_on"));
        }
        return true;
    }
}