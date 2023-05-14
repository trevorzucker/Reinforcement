package codes.zucker.reinforcement.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import codes.zucker.reinforcement.ReinforcementPlugin;

public class ConfigurationYaml {

    public static Map<String, Object> configValues = new HashMap<>();
    static File configFile;

    public static void createFile() {
        configFile = new File(JavaPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (FileOutputStream stream = new FileOutputStream(configFile)) {
                JavaPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder().mkdirs();
                configFile.createNewFile();
                InputStream def = JavaPlugin.getPlugin(ReinforcementPlugin.class).getResource("config.yml");
                byte[] buffer = new byte[def.available()];
                def.read(buffer);
                stream.write(buffer);
            } catch (IOException e) { }
        }
    }

    public static void loadConfigurationFile() {
        if (configFile == null) {
            configFile = new File(JavaPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder() + "/config.yml");
            if (!configFile.exists())
                createFile();
        }

        FileConfiguration config = JavaPlugin.getPlugin(ReinforcementPlugin.class).getConfig();
        for(String item : config.getKeys(false)) {
            if(config.getString(item) != null)
                configValues.put(item, config.get(item));
        }
    }

    public static String getString(String key) {
        return (String)configValues.get(key);
    }

    public static int getInt(String key) {
        return (int)configValues.get(key);
    }

    public static boolean getBoolean(String key) {
        return (boolean)configValues.get(key);
    }

    public static ArrayList<?> getList(String key) {
        return (ArrayList<?>)configValues.get(key);
    }
}