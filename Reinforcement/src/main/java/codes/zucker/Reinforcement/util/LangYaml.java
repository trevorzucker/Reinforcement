package codes.zucker.Reinforcement.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import codes.zucker.Reinforcement.ReinforcementPlugin;

public class LangYaml { // ConfigurationLoader, DataLoader, and LangLoader all work the same.

    public static Map<String, String> Values = new HashMap<String, String>();
    static File dataFile;
    static YamlConfiguration config;

    public static void CreateFile() {
        dataFile = new File(ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder() + "/lang.yml");
        if (!dataFile.exists())
            try {
                dataFile.createNewFile();
                InputStream def = ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getResource("lang.yml");
                FileOutputStream stream = new FileOutputStream(dataFile);
                int read;
                byte[] buffer = new byte[1024];
                while ((read = def.read(buffer)) != -1) {
                    stream.write(buffer, 0, read);
                }
                stream.close();
            } catch (IOException e) { }
    }

    public static void LoadLang() {
        if (dataFile == null) {
            dataFile = new File(ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder() + "/lang.yml");
            if (!dataFile.exists())
                CreateFile();
        }

        config = YamlConfiguration.loadConfiguration(dataFile);
        for(String item : config.getKeys(false)) {
            Values.put(item, config.getString(item).replaceAll("(&([a-z0-9]))", "\u00A7$2"));
        }
    }

    public static String GetString(String key) {
        return (String)Values.get(key);
    }
}