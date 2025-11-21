package codes.zucker.reinforcement.entity;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import codes.zucker.reinforcement.ReinforcementPlugin;

public class Hologram {

    protected static List<Hologram> holograms = new ArrayList<>();

    ArmorStand item;
    ArmorStand indicator;
    Location origin;
    Material lastHeadMaterial;
    Vector originOffset = new Vector(0, 0, 0);
    Vector indicatorOffset = new Vector(0, 1.225f, 0);
    int hideTimer;
    int doneHideTimer;
    int delayHideTimer;
    boolean hidden = true;
        
    public Hologram(Location location) {
        Location dest = location.clone().add(0.5f, -1.225f, 0.5f);

        origin = dest;

        item = origin.getWorld().spawn(origin, ArmorStand.class, armorStand -> {
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);
            armorStand.setVisible(false);
            armorStand.setSilent(true);
            armorStand.setInvulnerable(true);
            armorStand.setMarker(true);
            armorStand.setMetadata("isMarker", new FixedMetadataValue(JavaPlugin.getPlugin(ReinforcementPlugin.class), true));
        });

        indicator = origin.getWorld().spawn(origin.clone().add(indicatorOffset), ArmorStand.class, armorStand -> {
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);
            armorStand.setVisible(false);
            armorStand.setCustomNameVisible(false);
            armorStand.setSilent(true);
            armorStand.setInvulnerable(true);
            armorStand.setMarker(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMetadata("isMarker", new FixedMetadataValue(JavaPlugin.getPlugin(ReinforcementPlugin.class), true));
        });
        
        holograms.add(this);
    }

    public Hologram(Location location, Material material) {
        this(location);
        setHead(material);
    }

    public Hologram(Location location, Material material, String displayName) {
        this(location, material);
        indicator.setCustomName(displayName);
    }

    public void setHead(Material material) {
        ItemStack block = new ItemStack(material);

        EntityEquipment equip = item.getEquipment();
        equip.setHelmet(block);

        lastHeadMaterial = material;
    }

    public void showHologram() {
        setHead(lastHeadMaterial);
        indicator.setCustomNameVisible(true);
        Bukkit.getScheduler().cancelTask(hideTimer);
        Bukkit.getScheduler().cancelTask(doneHideTimer);
        hidden = false;
    }

    public void hideHologram() {

        hideTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(JavaPlugin.getPlugin(ReinforcementPlugin.class), () -> {
            if (item == null) return;
            setPositionRelative(originOffset.clone().multiply(0.1f));
        }, 0, 1);

        doneHideTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(ReinforcementPlugin.class), () -> {
            if (item == null) return;
            Bukkit.getScheduler().cancelTask(hideTimer);
            EntityEquipment entityEquipment = item.getEquipment();
            entityEquipment.setHelmet(null);
            indicator.setCustomNameVisible(false);
            originOffset = new Vector(0, 0, 0);
            hidden = true;
        }, 4);
    }

    public void hideHologramDelayed() {
        delayHideTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(ReinforcementPlugin.class), this::hideHologram, 40);
    }

    public void cancelHideDelayed() {
        Bukkit.getScheduler().cancelTask(delayHideTimer);
    }

    public void setPositionRelative(Vector offset) {
        Location destinationLocation = origin.clone();
        destinationLocation.add(offset);
        destinationLocation.setDirection(origin.clone().subtract(destinationLocation).toVector());
        item.teleport(destinationLocation);
        Location indicatorLocation = destinationLocation.clone();
        indicatorLocation.add(offset.clone().multiply(1.5f));
        indicatorLocation.add(indicatorOffset);
        indicator.teleport(indicatorLocation);
        originOffset = offset;
    }

    public void destroyHologram() {
        if (item != null) {
            item.remove();
            indicator.remove();
        }
        item = null;
        indicator = null;
    }

    public boolean getVisible() {
        return !hidden;
    }

    public void setText(String text) {
        indicator.setCustomName(text);
        indicator.setCustomNameVisible(!hidden);
    }

    public static List<Hologram> getHolograms() {
        return holograms;
    }
}
