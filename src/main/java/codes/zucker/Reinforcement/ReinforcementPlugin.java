package codes.zucker.reinforcement;

import java.lang.reflect.Constructor;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.File;
import java.lang.reflect.Modifier;


import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import codes.zucker.reinforcement.commands.CommandInfo;
import codes.zucker.reinforcement.commands.PluginCommand;
import codes.zucker.reinforcement.entity.Hologram;
import codes.zucker.reinforcement.util.ConfigurationYaml;
import codes.zucker.reinforcement.util.DataYaml;
import codes.zucker.reinforcement.util.LangYaml;

public class ReinforcementPlugin extends JavaPlugin
{

    private final Map<String, PluginCommand> commandHandlers = new HashMap<>();

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

        autoRegisterCommands();
    }
    
    @Override
    public void onDisable() {
        DataYaml.saveDataFile();
        Hologram.getHolograms().forEach(Hologram::destroyHologram);
    }

    private void autoRegisterCommands() {
        String basePackage = "codes.zucker.reinforcement.commands";
        String basePath = basePackage.replace('.', '/') + "/";

        int registered = 0;

        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) return;

        File jarFile;
        try {
            jarFile = new File(codeSource.getLocation().toURI());
        } catch (Exception e) {
            return;
        }

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(basePath) || !name.endsWith(".class")) continue;
                if (name.contains("$")) continue;

                String className = name
                        .substring(0, name.length() - 6)
                        .replace('/', '.');

                Class<?> clazz;
                try {
                    clazz = Class.forName(className, false, getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    continue;
                }

                if (!PluginCommand.class.isAssignableFrom(clazz)) continue;
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) continue;

                @SuppressWarnings("unchecked")
                Class<? extends PluginCommand> type = (Class<? extends PluginCommand>) clazz;

                CommandInfo info = type.getAnnotation(CommandInfo.class);
                if (info == null) continue;

                String cmdName = info.command().trim().toLowerCase();
                if (cmdName.isEmpty()) continue;

                try {
                    Constructor<? extends PluginCommand> ctor = type.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    PluginCommand instance = ctor.newInstance();

                    commandHandlers.put(cmdName, instance);
                    registered++;

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

        getLogger().info("Loaded " + registered + " command(s).");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String name = cmd.getName().toLowerCase();
        PluginCommand handler = commandHandlers.get(name);

        if (handler == null) {
            sender.sendMessage("Unknown command.");
            return true;
        }

        // Optional permission check using the annotation:
        CommandInfo info = handler.getClass().getAnnotation(CommandInfo.class);
        if (info != null && !info.permission().isEmpty()) {
            if (!sender.hasPermission(info.permission())) {
                sender.sendMessage("You don't have permission for that.");
                return true;
            }
        }

        return handler.execute(sender, cmd, label, args);
    }
}
