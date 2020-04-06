package codes.zucker.Reinforcement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {
    
    public static List<Player> playerToggle = new ArrayList<Player>();
    public static boolean riCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;
        if (playerToggle.contains(p))
            playerToggle.remove(p);
        else {
            playerToggle.add(p);
            p.sendMessage((String)LangLoader.Values.get("reinforcement_info_msg"));
        }
        return true;
    }
}