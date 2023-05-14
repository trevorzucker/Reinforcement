package codes.zucker.reinforcement.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import codes.zucker.reinforcement.Commands;

public class CommandHelper { // just some stuff to make creating commands easier
    
    public static Map<String, Method> commands = new HashMap<>();

    public static void registerCommand(String command, String methodName) {
        Method cmd = null;
        try {
            cmd = Commands.class.getMethod(methodName, CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException | SecurityException e) {LOG.severe("{}", e);}
        commands.put(command, cmd);
    }

    public static Player autoCompleteName(String name, Player caller) {
        Player p = null;

        for(Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getDisplayName().contains(name)) {
                if (caller == null || !pl.getUniqueId().equals(caller.getUniqueId())) {
                    p = pl;
                    break;
                }
            }
        }
        return p;
    }
}