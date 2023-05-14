package codes.zucker.Reinforcement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import codes.zucker.Reinforcement.util.LangYaml;
import codes.zucker.Reinforcement.util.Utils;

public class Commands {
    
    public static List<Player> riToggle = new ArrayList<Player>();
    public static boolean riCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;

        if (riToggle.contains(p)) {
            riToggle.remove(p);
            Utils.SendMessage(p, LangYaml.GetString("reinforcement_visibility_off"));
        }
        else {
            riToggle.add(p);
            Utils.SendMessage(p, LangYaml.GetString("reinforcement_visibility_on"));
        }
        return true;
    }

    public static List<Player> reToggle = new ArrayList<Player>();
    public static boolean reCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;

        if (reToggle.contains(p)) {
            reToggle.remove(p);
            Utils.SendMessage(p, LangYaml.GetString("reinforcement_mode_off"));
        }
        else {
            reToggle.add(p);
            Utils.SendMessage(p, LangYaml.GetString("reinforcement_mode_on"));
        }
        return true;
    }
}