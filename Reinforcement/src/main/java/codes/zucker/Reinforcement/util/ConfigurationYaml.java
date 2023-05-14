package codes.zucker.Reinforcement.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import codes.zucker.Reinforcement.ReinforcementPlugin;

public class ConfigurationYaml {

    public static Map<String, Object> ConfigValues = new HashMap<String, Object>();
    static File configFile;

    public static void CreateFile() {
        configFile = new File(ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder().mkdirs();
                configFile.createNewFile();
                InputStream def = ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getResource("config.yml");
                byte[] buffer = new byte[def.available()];
                def.read(buffer);
                FileOutputStream stream = new FileOutputStream(configFile);
                stream.write(buffer);
                stream.close();
            } catch (IOException e) { }
        }
    }

    public static void LoadConfigurationFile() {
        if (configFile == null) {
            configFile = new File(ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder() + "/config.yml");
            if (!configFile.exists())
                CreateFile();
        }

        FileConfiguration config = ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getConfig();
        for(String item : config.getKeys(false)) {
            if(config.getString(item) != null)
                ConfigValues.put(item, config.get(item));
        }
    }

    public static String GetString(String key) {
        return (String)ConfigValues.get(key);
    }

    public static int GetInt(String key) {
        return (int)ConfigValues.get(key);
    }

    public static boolean GetBoolean(String key) {
        return (boolean)ConfigValues.get(key);
    }

    public static ArrayList<?> GetList(String key) {
        return (ArrayList<?>)ConfigValues.get(key);
    }
}