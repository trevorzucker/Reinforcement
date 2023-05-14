package codes.zucker.Reinforcement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import codes.zucker.Reinforcement.entity.Hologram;
import codes.zucker.Reinforcement.util.CommandHelper;
import codes.zucker.Reinforcement.util.ConfigurationYaml;
import codes.zucker.Reinforcement.util.DataYaml;
import codes.zucker.Reinforcement.util.LOG;
import codes.zucker.Reinforcement.util.LangYaml;

public class ReinforcementPlugin extends JavaPlugin
{    

    @Override
    public void onEnable() {
        ConfigurationYaml.LoadConfigurationFile();

        ConfigurationYaml.GetList("reinforcement_blocks").forEach(i -> {
            List<?> entry = ((ArrayList<?>)i);
            Material material = Material.getMaterial((String)entry.get(0));
            int breaks = (int)entry.get(1);
            int max = (int)entry.get(2);
            ReinforceMaterial reinforceMaterial = new ReinforceMaterial(material, breaks, max);
            ReinforceMaterial.Entries.add(reinforceMaterial);
        });

        LangYaml.LoadLang();
        DataYaml.LoadDataFile();

        getServer().getPluginManager().registerEvents(new Events(), this);

        CommandHelper.RegisterCommand("ri", "riCommand");
        CommandHelper.RegisterCommand("re", "reCommand");
    }
    
    @Override
    public void onDisable() {
        DataYaml.SaveDataFile();
        Hologram.holograms.forEach(i -> {
            i.Destroy();
        });
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
