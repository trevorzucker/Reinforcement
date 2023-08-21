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
        Method commandMethod = null;
        try {
            commandMethod = Commands.class.getMethod(methodName, CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException | SecurityException e) {LOG.severe("{}", e);}
        commands.put(command, commandMethod);
    }

    public static Player autoCompleteName(String name, Player caller) {
        Player player = null;

        for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if ((onlinePlayer.getDisplayName().contains(name)) && (caller == null || !onlinePlayer.getUniqueId().equals(caller.getUniqueId()))) {
                player = onlinePlayer;
                break;
            }
        }
        return player;
    }
}