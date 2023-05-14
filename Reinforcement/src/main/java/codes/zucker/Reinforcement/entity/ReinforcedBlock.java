package codes.zucker.reinforcement.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import codes.zucker.reinforcement.ReinforceMaterial;
import codes.zucker.reinforcement.util.ConfigurationYaml;
import codes.zucker.reinforcement.util.Utils;

public class ReinforcedBlock {

    public static Map<Material, Entry<Integer, Integer>> Materials = new HashMap<>();
    public static Map<Location, ReinforcedBlock> BlockList = new HashMap<>();

    private Location blockLocation;
    private int breaksLeft;
    private ReinforceMaterial materialUsed;
    private Hologram hologram;
    private boolean destroyed = false;
    private UUID owner;

    private List<Hologram> hologramList = new ArrayList<>();

    public ReinforcedBlock(Location src, int breaksLeft, ReinforceMaterial materialUsed, UUID owner) {
        if (src.getBlock().getType() == Material.AIR) return;
        blockLocation = src;
        this.breaksLeft = breaksLeft;
        this.materialUsed = materialUsed;
        BlockList.put(src, this);
        hologram = new Hologram(blockLocation, materialUsed.getMaterial());
        this.owner = owner;

        displayNearest(src);
    }
    
    public static ReinforcedBlock getAtLocation(Location src) {
        return BlockList.get(src);
    }
    
    public void damageBlock(Location source, int times) {
        setBreaksLeft(getBreaksLeft() - times);

        if (destroyed) return;

        displayNearest(source);
    }

    public void destroyBlock() {
        destroyBlock(true);
    }

    public void destroyBlock(boolean tryDropItem) {
        destroyed = true;
        hologram.destroyHologram();

        if (tryDropItem) {
            Random r = new Random();
            int chance = (int)(r.nextFloat() * 100f);
            int configChance = ConfigurationYaml.getInt("reinforcement_block_drop_chance");
            if (chance <= configChance)
                blockLocation.getWorld().dropItemNaturally(blockLocation, new ItemStack(materialUsed.getMaterial(), 1));
        }
        blockLocation = null;

        BlockList.values().remove(this);
    }

    public void setBreaksLeft(int breaks) {
        breaksLeft = breaks;
        if (breaksLeft <= 0) {
            getLocation().getWorld().playSound(getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 0.5f);
            destroyBlock();
        }
    }

    public void reinforceBlock(int amountToReinforce) {
        setBreaksLeft((int)Utils.clamp(getBreaksLeft() + amountToReinforce, 0, materialUsed.getMaxAllowedBreaks()));
        blockLocation.getWorld().playSound(blockLocation, Sound.BLOCK_GLASS_PLACE, 0.8f, 2);
        formatHologramName();
    }

    void formatHologramName() {
        hologram.setText(Utils.greenToRed(getBreaksLeft() + "", getBreaksLeft(), materialUsed.getMaxAllowedBreaks()));
    }

    public void displayNearest(Location src) {
        displayNearest(src, true);
    }

    public void displayNearest(Location src, boolean hide) {
        hologram.showHologram();
        formatHologramName();
        final Vector vec = Utils.getClosestBlockFaceToLocationVisible(src, getLocation());
        getHologram().setPositionRelative(vec);

        hologram.cancelHideDelayed();

        if (hide)
            hologram.hideHologramDelayed();
    }

    public int getBreaksLeft() {
        return breaksLeft;
    }

    public ReinforceMaterial getMaterialUsed() {
        return materialUsed;
    }

    public Location getLocation() {
        return blockLocation;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public List<Hologram> getHologramList() {
        return hologramList;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}