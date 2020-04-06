package codes.zucker.Reinforcement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class Utils { // some nice utils

    public static List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<Block>();
        for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                   blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static void RemoveFromHand(PlayerInventory i, Material item, int quantity) {
        ItemStack hand = i.getItemInMainHand();
        if (hand.getType().equals(item)) {
            hand.setAmount(hand.getAmount() - quantity);
            return;
        }
        for(ItemStack stack : i.getContents()) {
            if (stack != null && stack.getType() == item) {
                stack.setAmount(stack.getAmount() - quantity);
                break;
            }
        }
    }

    
    static Material[] NeighborBlacklist = new Material[] {
        Material.WATER,
        Material.LAVA,
        Material.CAVE_AIR,
        Material.AIR,
        Material.FIRE,
    };

    // checks against the given block and it's vector offset to see if there's a block in it's place
    // helpful for optimizing ReinforcedBlock's armor stands, so we only display on sides that are visible
    public static boolean HasNeighbor(Block b, Vector offset) {
        Location l = b.getLocation().clone();
        l.add(offset.getX(), offset.getY(), offset.getZ());
        Block neighbor = b.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        for(Material m : NeighborBlacklist) {
            if (m.equals(neighbor.getType()))
                return false;
        }
        return true;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    static Vector[] faceOffsets = new Vector[] {
        new Vector(0.8f, 0, 0),
        new Vector(-0.8f, 0, 0),
        new Vector(0, 0.8f, 0),
        new Vector(0, -0.8f, 0),
        new Vector(0, 0, 0.8f),
        new Vector(0, 0, -0.8f),
    };

    // gets the vector of the given block's closest face to the given location
    // this is used in conjunction with HasNeighbor to return the closest VISIBLE side.
    public static Vector GetClosestBlockFaceToLocationVisible(Location loc, Block b) {
        Location bCenter = b.getLocation(); bCenter.add(0.5f, 0.5f, 0.5f);
        Vector closestFace = faceOffsets[0];
        double closestDist = Double.MAX_VALUE;
        for(int i = 0; i < 6; i++) {
            Vector offset = faceOffsets[i].clone(); offset.multiply(1.25f);
            if (HasNeighbor(b, offset)) continue;
            Location face = bCenter.clone(); face.add(faceOffsets[i]);
            double dist = loc.distance(face);
            if (dist < closestDist) {
                closestDist = dist;
                closestFace = faceOffsets[i];
            }
        }
        return closestFace;
    }
}