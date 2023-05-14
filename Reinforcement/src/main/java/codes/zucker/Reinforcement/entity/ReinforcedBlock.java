package codes.zucker.Reinforcement.entity;

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

import codes.zucker.Reinforcement.ReinforceMaterial;
import codes.zucker.Reinforcement.util.ConfigurationYaml;
import codes.zucker.Reinforcement.util.Utils;

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

        DisplayNearest(src);
    }
    
    public static ReinforcedBlock GetAtLocation(Location src) {
        return BlockList.get(src);
    }
    
    public void Break(Location source, int times) {
        Break(source, times, 14);
    }

    // "nice" do damage method
    public void Break(Location source, int times, int ticks) {
        setBreaksLeft(getBreaksLeft() - times);

        if (destroyed) return;

        DisplayNearest(source);
    }

    public void Destroy() {
        Destroy(true);
    }

    public void Destroy(boolean tryDropItem) {
        destroyed = true;
        hologram.Destroy();

        if (tryDropItem) {
            Random r = new Random();
            int chance = (int)(r.nextFloat() * 100f);
            int configChance = ConfigurationYaml.GetInt("reinforcement_block_drop_chance");
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
            Destroy();
        }
    }

    public void Reinforce(int amountToReinforce) {
        setBreaksLeft((int)Utils.clamp(getBreaksLeft() + amountToReinforce, 0, materialUsed.getMaxAllowedBreaks()));
        blockLocation.getWorld().playSound(blockLocation, Sound.BLOCK_GLASS_PLACE, 0.8f, 2);
        formatHologramName();
    }

    void formatHologramName() {
        hologram.setText(Utils.GreenToRed(getBreaksLeft() + "", getBreaksLeft(), materialUsed.getMaxAllowedBreaks()));
    }

    public void DisplayNearest(Location src) {
        DisplayNearest(src, true);
    }

    public void DisplayNearest(Location src, boolean hide) {
        hologram.Show();
        formatHologramName();
        final Vector vec = Utils.GetClosestBlockFaceToLocationVisible(src, getLocation());
        getHologram().SetPositionRelative(vec);

        hologram.CancelHideDelayed();

        if (hide)
            hologram.HideDelayed(40);
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