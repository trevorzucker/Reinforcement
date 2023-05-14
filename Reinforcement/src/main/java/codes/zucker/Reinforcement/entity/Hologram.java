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
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import codes.zucker.reinforcement.ReinforcementPlugin;

public class Hologram {

    public static List<Hologram> holograms = new ArrayList<>();

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
        
    public Hologram(Location src) {
        Location dest = src.clone().add(0.5f, -1.225f, 0.5f);

        origin = dest;

        item = origin.getWorld().spawn(origin, ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand t) {
                t.setGravity(false);
                t.setCanPickupItems(false);
                t.setVisible(false);
                t.setSilent(true);
                t.setInvulnerable(true);
                t.setMarker(true);
                t.setMetadata("isMarker", new FixedMetadataValue(JavaPlugin.getPlugin(ReinforcementPlugin.class), true));
            }
        });

        indicator = (ArmorStand)origin.getWorld().spawn(origin.clone().add(indicatorOffset), ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand t) {
                t.setGravity(false);
                t.setCanPickupItems(false);
                t.setVisible(false);
                t.setCustomNameVisible(false);
                t.setSilent(true);
                t.setInvulnerable(true);
                t.setMarker(true);
                t.setCustomNameVisible(true);
                t.setMetadata("isMarker", new FixedMetadataValue(JavaPlugin.getPlugin(ReinforcementPlugin.class), true));
            }
        });
        
        holograms.add(this);
    }

    public Hologram(Location src, Material material) {
        this(src);
        setHead(material);
    }

    public Hologram(Location src, Material material, String display) {
        this(src, material);
        indicator.setCustomName(display);
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

        hideTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(JavaPlugin.getPlugin(ReinforcementPlugin.class), new Runnable() {

            @Override
            public void run() {
                if (item == null) return;
                setPositionRelative(originOffset.clone().multiply(0.1f));
            }
        }, 0, 1);

        doneHideTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(ReinforcementPlugin.class), new Runnable() {

            @Override
            public void run() {
                if (item == null) return;
                Bukkit.getScheduler().cancelTask(hideTimer);
                EntityEquipment equip = item.getEquipment();
                equip.setHelmet(null);
                indicator.setCustomNameVisible(false);
                originOffset = new Vector(0, 0, 0);
                hidden = true;
            }

        }, 4);
    }

    public void hideHologramDelayed() {
        delayHideTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(ReinforcementPlugin.class), new Runnable() {

            @Override
            public void run() {
                hideHologram();
            }

        }, 40);
    }

    public void cancelHideDelayed() {
        Bukkit.getScheduler().cancelTask(delayHideTimer);
    }

    public void setPositionRelative(Vector offset) {
        Location dest = origin.clone();
        dest.add(offset);
        dest.setDirection(origin.clone().subtract(dest).toVector());
        item.teleport(dest);
        Location indicatorLoc = dest.clone();
        indicatorLoc.add(offset.clone().multiply(1.5f));
        indicatorLoc.add(indicatorOffset);
        indicator.teleport(indicatorLoc);
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
}
