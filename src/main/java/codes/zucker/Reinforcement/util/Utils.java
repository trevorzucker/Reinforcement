package codes.zucker.reinforcement.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class Utils {

    static final Material[] neighborBlacklist = new Material[] {
        Material.WATER,
        Material.LAVA,
        Material.CAVE_AIR,
        Material.AIR,
        Material.FIRE,
    };

    static final Vector[] neighborOffsets = new Vector[] {
        new Vector(1, 0, 0),
        new Vector(-1, 0, 0),
        new Vector(0, 1, 0),
        new Vector(0, -1, 0),
        new Vector(0, 0, 1),
        new Vector(0, 0, -1),
    };

    static final Vector[] faceOffsets = new Vector[] {
        new Vector(0.3f, 0, 0),
        new Vector(-0.3f, 0, 0),
        new Vector(0, 0.3f, 0),
        new Vector(0, -0.3f, 0),
        new Vector(0, 0, 0.3f),
        new Vector(0, 0, -0.3f),
    };

    public static Random random = new Random();

    public static List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<>();
        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                   blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static List<Player> getPlayersNear(Location location, int radius) {
        List<Player> playerList = new ArrayList<>();
        Bukkit.getServer().getOnlinePlayers().forEach(p -> {
            double distance = location.distance(p.getLocation());
            if (distance <= radius) {
                playerList.add(p);
            }
        });
        return playerList;
    }

    public static void removeFromHand(PlayerInventory inventory, Material item, int quantity) {
        ItemStack itemInHand = inventory.getItemInMainHand();
        if (itemInHand.getType().equals(item)) {
            itemInHand.setAmount(itemInHand.getAmount() - quantity);
            return;
        }
        for(ItemStack stack : inventory.getContents()) {
            if (stack != null && stack.getType() == item) {
                stack.setAmount(stack.getAmount() - quantity);
                break;
            }
        }
    }

    // checks against the given block and it's offset to see if there's a block in it's place
    // helpful for optimizing ReinforcedBlock's armor stands, so we only display on sides that are visible
    public static boolean hasNeighbor(Block block, Vector offset) {
        Location location = block.getLocation().clone();
        location.add(offset);
        Block neighbor = location.getBlock();
        for(Material material : neighborBlacklist) {
            if (material.equals(neighbor.getType()))
                return false;
        }
        return true;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    // gets the vector of the given block's closest face to the given location
    // this is used in conjunction with HasNeighbor to return the closest VISIBLE side.
    public static Vector getClosestBlockFaceToLocationVisible(Location loc, Location src) {
        Location blockCenter = src.clone(); blockCenter.add(0.5f, 0.5f, 0.5f);
        Vector closestFace = faceOffsets[0];
        double closestDist = Double.MAX_VALUE;
        for(int i = 0; i < 6; i++) {
            Vector offset = neighborOffsets[i].clone();
            if (hasNeighbor(src.getBlock(), offset)) {
                continue;
            }
            Location face = blockCenter.clone(); face.add(faceOffsets[i]);
            double dist = loc.distance(face);
            if (dist < closestDist) {
                closestDist = dist;
                closestFace = faceOffsets[i];
            }
        }
        return closestFace;
    }

    public static void sendMessage(Player p, String message) {
        p.sendMessage(ChatColor.GREEN + LangYaml.getString("reinforcement_message_prefix") + " " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static String greenToRed(String content, int value, int max) {
        ChatColor color = ChatColor.RED;
        if (value > max * 0.75f)
            color = ChatColor.GREEN;
        else if (value > max * 0.4f)
            color = ChatColor.YELLOW;
        return (color + content);
    }
}