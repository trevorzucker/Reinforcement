package codes.zucker.Reinforcement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

public class ReinforcedBlock {

    public static Material block = Material.getMaterial((String) ConfigurationLoader.ConfigValues.get("reinforcement_block"));
    public static Map<Location, ReinforcedBlock> list = new HashMap<Location, ReinforcedBlock>();

    private Location blockLocation;
    private int breaksLeft = 1;
    private List<ArmorStand> blockStands = new ArrayList<ArmorStand>();

    public ReinforcedBlock(Block b, int breaksLeft) {
        if (b.getType() == Material.AIR) return;
        World w = b.getLocation().getWorld();
        Vector pos = b.getLocation().toVector();
        Location loc = new Location(w, pos.getX(), pos.getY(), pos.getZ());
        blockLocation = loc;
        this.breaksLeft = breaksLeft;
        list.put(blockLocation, this);
    }

    public int GetBreaksLeft() { return breaksLeft; }

    // do damage method
    public void SetBreaksLeft(int breaks) {
        breaksLeft = breaks;
        if (breaksLeft <= 0) {
            GetLocation().getWorld().playSound(GetLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 0.5f);
            Dispose();
        }
    }

    public void Break(Location source, int times) {
        Break(source, times, 14);
    }

    // "nice" do damage method
    public void Break(Location source, int times, int ticks) {
        SetBreaksLeft(GetBreaksLeft() - times);
        if (GetBreaksLeft() <= 0) return;

        clearBreaksLeft(source, true);
        if (!indicatorsVisible())
            displayBreaksLeft(source, ticks);
        else
            displayBreaksLeft(source, -1);
    }

    public Location GetLocation() { return blockLocation; }

    // For when ReinforcedBlock object is getting removed!
    public void Dispose() {
        removeAllIndicators(true);
        clearBreaksLeft(null, true);
        list.remove(blockLocation);

        Random r = new Random();
        int chance = (int)(r.nextFloat() * 100f);
        int configChance = (int)ConfigurationLoader.ConfigValues.get("reinforcement_block_drop_chance");
        if (chance <= configChance)
            blockLocation.getWorld().dropItemNaturally(blockLocation, new ItemStack(block, 1));
        blockLocation = null;
    }

    // I used a map for optimization so we can get by key for this.
    public static ReinforcedBlock GetAtLocation(Location loc) {
        loc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return list.get(loc);
    }

    Vector[] offsets = new Vector[] {
        new Vector(0.4f, 0, 0),
        new Vector(-0.4f, 0, 0),
        new Vector(0, 0.4f, 0),
        new Vector(0, -0.4f, 0),
        new Vector(0, 0, 0.4f),
        new Vector(0, 0, -0.4f)
    };

    // for creating the "indicators", aka the blocks that pop out of the reinforced blocks
    public void createArmorStandIndicators(boolean instant) {
        Location _loc = blockLocation.clone();
        Block b = blockLocation.getBlock();
        _loc.add(new Vector(0.5f, -1.2f, 0.5f));

        ItemStack chest = new ItemStack(ReinforcedBlock.block, 1);

        ArmorStand[] stands = new ArmorStand[6];

        for(int j = 0; j < 6; j++) {
            Location loc = _loc.clone();
            if (instant) {
                loc.add(offsets[j]);
                Vector offset = offsets[j].clone(); offset.multiply(2.5f);
                if(Utils.HasNeighbor(b, offset)) continue;
            }

            ArmorStand stand = (ArmorStand) b.getLocation().getWorld().spawn(loc, ArmorStand.class, new Consumer<ArmorStand>() {
                @Override
                public void accept(ArmorStand t) {
                    t.setGravity(false);
                    t.setCanPickupItems(false);
                    t.setVisible(false);
                    t.setSilent(true);
                    t.setInvulnerable(true);
                    t.setMarker(true);
                    t.setMetadata("isMarker", new FixedMetadataValue(Main.getPlugin(Main.class), true));
                }
            });
            EntityEquipment equip = stand.getEquipment();
            equip.setHelmet(chest);
            stands[j] = stand;
        }

        blockStands.addAll(Arrays.asList(stands));
        if (instant) return;

        final ArmorStand[] finalStands = stands;
        final int timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 6; i++) {
                    if (finalStands[i] == null) continue;
                    ArmorStand stand = finalStands[i];
                    Location loc = stand.getLocation().clone();
                    Vector offset = offsets[i].clone(); offset.multiply(0.1f);
                    loc.add(offset);
                    Vector offsetNeighbor = offsets[i].clone(); offsetNeighbor.multiply(2.5f);
                    if(Utils.HasNeighbor(b, offsetNeighbor)) continue;
                    stand.teleport(loc);
                }
            }
        }, 0, 1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable(){
        
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(timer);
            }
        }, 10);
    }

    // for removing all the indicators
    public void removeAllIndicators(boolean instant) {
        if (blockStands.size() <= 0) return;
        if (instant) {
            for(ArmorStand stand : blockStands) {
                if (stand != null)
                    stand.remove();
            }
            blockStands.clear();
            return;
        }
        final List<ArmorStand> stands = new ArrayList<ArmorStand>(blockStands);

        blockStands.clear();

        final int timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 6; i++) {
                    if (stands.get(i) == null) continue;
                    ArmorStand stand = stands.get(i);
                    Location loc = stand.getLocation().clone();
                    Vector offset = offsets[i].clone(); offset.multiply(0.1f); offset.multiply(-1);
                    loc.add(offset);
                    //Vector offsetNeighbor = offsets[i].clone(); offset.multiply(2.5f);
                    //if(Utils.HasNeighbor(blockLocation.getBlock(), offsetNeighbor)) continue;
                    stand.teleport(loc);
                }
            }
        }, 0, 1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable(){
        
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(timer);
                for(ArmorStand stand : stands) {
                    if (stand != null)
                        stand.remove();
                }
            }
        }, 6);
    }

    public boolean indicatorsVisible() {
        if (blockStands.size() > 0) return true;
        return false;
    }

    List<ArmorStand> breakIndicators = new ArrayList<ArmorStand>();

    // for creating the damage remaining hint
    public void displayBreaksLeft(Location source, long ticks) {
        Location standLocation = blockLocation.clone();
        standLocation.add(0.5f, 0.1f, 0.5f);
        Vector closest = Utils.GetClosestBlockFaceToLocationVisible(source, blockLocation.getBlock());

        standLocation.add(closest);

        ArmorStand stand = (ArmorStand) blockLocation.getWorld().spawn(standLocation, ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand t) {
                t.setGravity(false);
                t.setCanPickupItems(false);
                t.setVisible(false);
                t.setSilent(true);
                t.setInvulnerable(true);
                t.setMarker(true);
                t.setMarker(true);
                t.setSilent(true);
                t.setCanPickupItems(false);
                t.setMetadata("isMarker", new FixedMetadataValue(Main.getPlugin(Main.class), true));
            }
        });

        int maxBreaks = (int) ConfigurationLoader.ConfigValues.get("reinforcement_break_counter");
        float pct = (breaksLeft / (float)maxBreaks) * 100;
        ChatColor c = ChatColor.GREEN;
        if (pct < 65 && pct >= 35)
            c = ChatColor.YELLOW;
        if (pct < 35) {
            c = ChatColor.RED;
        }
        stand.setCustomName(c + "" + breaksLeft);
        stand.setCustomNameVisible(true);
        breakIndicators.add(stand);


        if (ticks == -1) return;

        final ArmorStand finalStand = stand;
        final Location finalLoc = standLocation.clone();
        final long finalTicks = ticks;
        final Vector finalClosest = closest.clone();
        finalClosest.multiply(0.5f);
        
        final int repeating = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable(){
        
            @Override
            public void run() {
                finalLoc.subtract(finalClosest.getX() / finalTicks, finalClosest.getY() / finalTicks, finalClosest.getZ() / finalTicks);
                finalStand.teleport(finalLoc);
            }
        }, 0, 1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable(){
        
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(repeating);
                breakIndicators.remove(finalStand);
                finalStand.remove();
            }
        }, ticks);
    }

    public long lastClearTime = 0;

    // for removing the damage hints
    public void clearBreaksLeft(Location src, boolean instant) {
        lastClearTime = System.currentTimeMillis();
        for(int i = 0; i < breakIndicators.size(); i++) {
            ArmorStand stand = breakIndicators.get(i);
            if (instant) {
                breakIndicators.remove(stand);
                stand.remove();
                continue;
            }

            final ArmorStand finalStand = stand;
            final Location finalLoc = stand.getLocation().clone();
            final Vector finalClosest = Utils.GetClosestBlockFaceToLocationVisible(src, blockLocation.getBlock());

            final int repeating = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable(){
        
                @Override
                public void run() {
                    finalLoc.subtract(finalClosest.getX() / 14, finalClosest.getY() / 14, finalClosest.getZ() / 14);
                    finalStand.teleport(finalLoc);
                }
            }, 10, 1);
    
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable(){
            
                @Override
                public void run() {
                    Bukkit.getScheduler().cancelTask(repeating);
                    breakIndicators.remove(finalStand);
                    finalStand.remove();
                }
            }, 16);

        }
    }
}