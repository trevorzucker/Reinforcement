package codes.zucker.Reinforcement;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

class DataLoader { // ConfigurationLoader, DataLoader, and LangLoader all work the same.

    public static Map<String, Object> DataValues = new HashMap<String, Object>();
    static File dataFile;
    static YamlConfiguration config;

    public static void CreateFile() {
        dataFile = new File(Main.getPlugin(Main.class).getDataFolder() + "/data.yml");
        if (!dataFile.exists())
            try {
                dataFile.createNewFile();
            } catch (IOException e) { }
    }

    public static void LoadDataFile() {
        CreateFile();
        config = YamlConfiguration.loadConfiguration(dataFile);
        for (String coords : config.getKeys(false)) {
            String breaksLeft = config.getString(coords);
            String[] locStr = coords.split("_");
            Location loc = new Location(Bukkit.getWorld(locStr[0]), Double.parseDouble(locStr[1]), Double.parseDouble(locStr[2]), Double.parseDouble(locStr[3]));
            new ReinforcedBlock(loc.getBlock(), Integer.parseInt(breaksLeft));
            DataValues.put(coords, breaksLeft);
        }
    }

    public static void SaveDataFile() {

        for (Entry<Location, ReinforcedBlock> entry : ReinforcedBlock.list.entrySet()) {
            Location location = entry.getKey();
            String loc = location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
            String val = entry.getValue().GetBreaksLeft() + "";
            config.set(loc, val);
        }

        try { config.save(dataFile); } catch (IOException e) { }
    }
}