package codes.zucker.Reinforcement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

class ConfigurationLoader { // ConfigurationLoader, DataLoader, and LangLoader all work the same.

    public static Map<String, Object> ConfigValues = new HashMap<String, Object>();
    static File configFile;

    public static void CreateFile() {
        configFile = new File(Main.getPlugin(Main.class).getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                Main.getPlugin(Main.class).getDataFolder().mkdirs();
                configFile.createNewFile();
                InputStream def = Main.getPlugin(Main.class).getResource("config.yml");
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
            configFile = new File(Main.getPlugin(Main.class).getDataFolder() + "/config.yml");
            if (!configFile.exists())
                CreateFile();
        }

        FileConfiguration config = Main.getPlugin(Main.class).getConfig();
        for(String item : config.getKeys(false)) {
            if(config.getString(item) != null)
            ConfigValues.put(item, config.get(item));
        }
    }
}