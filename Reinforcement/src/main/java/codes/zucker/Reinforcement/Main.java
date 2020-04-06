package codes.zucker.Reinforcement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigurationLoader.LoadConfigurationFile(); // Load
        LangLoader.LoadLang();                      // our
        DataLoader.LoadDataFile();                 // files

        getServer().getPluginManager().registerEvents(new Events(), this);

        CommandHelper.RegisterCommand("ri", "riCommand");
    }

    @Override
    public void onDisable() {
        DataLoader.SaveDataFile();

        // Remove all existing armor stands, just in case
        // All armor stands are given a metadata value upon creation, "isMarker".
        for(World w : Bukkit.getWorlds()) {
            for(Entity e : w.getEntities()) {
                if (!(e instanceof ArmorStand)) continue;
                if (e.hasMetadata("isMarker")) // if they have our metadata, aka OUR armorstand,
                    e.remove(); // remove it.
            }
        }
    }

    // Command handler

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (Map.Entry<String, Method> map : CommandHelper.commands.entrySet()) {
            if (cmd.getName().equalsIgnoreCase(map.getKey())) {
                try {
                    // Here's some dodgy reflection stuff...
                    Method meth = map.getValue();
                    meth.invoke(null, sender, cmd, label, args);
                    return true;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                }
                return true;
            }
        }

        return false;
    }
}