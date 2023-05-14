package codes.zucker.reinforcement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import codes.zucker.reinforcement.entity.Hologram;
import codes.zucker.reinforcement.util.CommandHelper;
import codes.zucker.reinforcement.util.ConfigurationYaml;
import codes.zucker.reinforcement.util.DataYaml;
import codes.zucker.reinforcement.util.LOG;
import codes.zucker.reinforcement.util.LangYaml;

public class ReinforcementPlugin extends JavaPlugin
{    

    @Override
    public void onEnable() {
        ConfigurationYaml.loadConfigurationFile();

        ConfigurationYaml.getList("reinforcement_blocks").forEach(i -> {
            List<?> entry = ((ArrayList<?>)i);
            Material material = Material.getMaterial((String)entry.get(0));
            int breaks = (int)entry.get(1);
            int max = (int)entry.get(2);
            ReinforceMaterial reinforceMaterial = new ReinforceMaterial(material, breaks, max);
            ReinforceMaterial.entries.add(reinforceMaterial);
        });

        LangYaml.loadLang();
        DataYaml.loadDataFile();

        getServer().getPluginManager().registerEvents(new Events(), this);

        CommandHelper.registerCommand("rv", "rvCommand");
        CommandHelper.registerCommand("re", "reCommand");
    }
    
    @Override
    public void onDisable() {
        DataYaml.saveDataFile();
        Hologram.holograms.forEach(Hologram::destroyHologram);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (Map.Entry<String, Method> map : CommandHelper.commands.entrySet()) {
            if (cmd.getName().equalsIgnoreCase(map.getKey())) {
                try {
                    // Here's some dodgy reflection stuff...
                    Method meth = map.getValue();
                    meth.invoke(null, sender, cmd, label, args);
                    return true;
                } catch (Exception e) {
                    LOG.severe("{}", e);
                }
                return true;
            }
        }

        return false;
    }
}
