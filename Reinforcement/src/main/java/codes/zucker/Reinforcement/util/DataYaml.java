package codes.zucker.Reinforcement.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import codes.zucker.Reinforcement.ReinforceMaterial;
import codes.zucker.Reinforcement.ReinforcementPlugin;
import codes.zucker.Reinforcement.entity.ReinforcedBlock;

public class DataYaml { // ConfigurationLoader, DataLoader, and LangLoader all work the same.

    static File dataFile;
    static YamlConfiguration config;

    public static void CreateFile() {
        dataFile = new File(ReinforcementPlugin.getPlugin(ReinforcementPlugin.class).getDataFolder() + "/data.yml");
        if (!dataFile.exists())
            try {
                dataFile.createNewFile();
            } catch (IOException e) { }
    }

    public static void LoadDataFile() {
        CreateFile();
        config = YamlConfiguration.loadConfiguration(dataFile);
        for (String world : config.getKeys(false)) {
            ConfigurationSection worldConf = config.getConfigurationSection(world);
            Set<String> val = worldConf.getKeys(true);

            Map<String, String> worldData = new HashMap<>();

            for(String s : val) {
                if (s.contains("]")) {

                    String blockData = worldConf.getString(s);

                    s = s.replaceAll("\\[|\\]", "");
                    blockData = blockData.replaceAll("\\[|\\]", "");

                    worldData.put(s, blockData);
                }
            }

            for(Entry<String, String> dataSet : worldData.entrySet()) {
                String[] coords = dataSet.getKey().split(", ");
                String[] data = dataSet.getValue().split(", ");
                Location location = new Location(Bukkit.getWorld(world), Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
                UUID owner = UUID.fromString(data[2]);
                new ReinforcedBlock(location, Integer.parseInt(data[0]), ReinforceMaterial.GetFromMaterial(data[1]), owner);
            }
        }
    }

    public static void SaveDataFile() {

        Set<String> worlds = new HashSet<>();

        for (Entry<Location, ReinforcedBlock> entry : ReinforcedBlock.BlockList.entrySet()) {
            Location location = entry.getValue().getLocation();
            String worldName = location.getWorld().getName();
            worlds.add(worldName);
        }

        
        for(String world : worlds) {
            Map<String, String> worldBlocks = new HashMap<>();
            for(Entry<Location, ReinforcedBlock> blockEntry : ReinforcedBlock.BlockList.entrySet()) {
                if (blockEntry.getValue().getLocation().getWorld().getName().equals(world)) {
                    Location src = blockEntry.getValue().getLocation();
                    String location = "[" + src.getX() + ", " + src.getY() + ", " + src.getZ() + "]";
                    String dataString = "[" + blockEntry.getValue().getBreaksLeft() + ", " + blockEntry.getValue().getMaterialUsed().getMaterial().name()
                        + ", " + blockEntry.getValue().getOwner() + "]";
                    worldBlocks.put(location, dataString);
                }
            }
            config.set(world, worldBlocks);
        }

        try { config.save(dataFile); } catch (IOException e) { }
    }
}