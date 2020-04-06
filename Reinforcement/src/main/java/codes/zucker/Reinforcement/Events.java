package codes.zucker.Reinforcement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class Events implements Listener {

    @EventHandler
    public static void onClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        PlayerInventory i = p.getInventory();
        Material hand = i.getItemInMainHand().getType();
        Block b = event.getClickedBlock();

        if (!hand.equals(ReinforcedBlock.block))
            return;

        if (event.getAction() != Action.LEFT_CLICK_BLOCK || ReinforcedBlock.GetAtLocation(event.getClickedBlock().getLocation()) != null )
            return;

        // quick check to disallow reinforcing all blacklisted blocks in the config
        String materialName = event.getClickedBlock().getType().toString();
        for(String s : (ArrayList<String>)ConfigurationLoader.ConfigValues.get("reinforcement_not_reinforceable")) {
            if (materialName.contains(s) && !materialName.contains("BLOCK"))
                return;
        }

        Utils.RemoveFromHand(i, ReinforcedBlock.block, 1);

        Random r = new Random();
        DustOptions particle = new DustOptions(Color.GRAY, 0.5f);
        for (int j = 0; j < r.nextInt(10) + 30; j++) { // fancy particles
            Location l = b.getLocation().clone();
            l.add(r.nextDouble() * 2f - 0.5f, r.nextDouble() * 2f - 0.5f, r.nextDouble() * 2f - 0.5f);
            p.getWorld().spawnParticle(Particle.REDSTONE, l, 5, particle);
        }

        p.getWorld().playSound(b.getLocation(), Sound.BLOCK_GLASS_PLACE, 0.8f, 2);

        ReinforcedBlock reinforced = new ReinforcedBlock(b, (int)ConfigurationLoader.ConfigValues.get("reinforcement_break_counter"));
        reinforced.createArmorStandIndicators(true);
        reinforced.displayBreaksLeft(p.getEyeLocation(), -1);

        event.setCancelled(true);
    }

    static Map<Player, List<Block>> playerVisibleBlocks = new HashMap<Player, List<Block>>();

    @EventHandler
    public static void playerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        Location loc = p.getLocation();
        List<Block> away = Utils.getNearbyBlocks(loc, 8);
        for (Block _b : away) {
            ReinforcedBlock b = ReinforcedBlock.GetAtLocation(_b.getLocation());
            if (b == null || !b.indicatorsVisible())
                continue;
            boolean otherPlayerSees = false;
            for (Entry<Player, List<Block>> entry : playerVisibleBlocks.entrySet()) {
                for (Block bl : entry.getValue()) {
                    if (bl.equals(_b))
                        otherPlayerSees = true;
                }
            }

            if (otherPlayerSees)
                continue;

            b.removeAllIndicators(false);
            b.clearBreaksLeft(p.getEyeLocation(), false);
        }

        boolean playerInfoMode = false;
        for(Player pl : Commands.playerToggle) {
            if (p.equals(pl)) {
                playerInfoMode = true;
                break;
            }
        }

        if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(ReinforcedBlock.block) && !playerInfoMode) {
            playerVisibleBlocks.remove(event.getPlayer());
            return;
        }

        List<Block> nearby = Utils.getNearbyBlocks(loc, 5);

        for (Block _b : nearby) {
            ReinforcedBlock b = ReinforcedBlock.GetAtLocation(_b.getLocation());
            if (b != null) {
                if (!b.indicatorsVisible()) {
                    b.createArmorStandIndicators(false);
                }
                
                if (b.lastClearTime + 300 < System.currentTimeMillis()) {
                    b.clearBreaksLeft(p.getEyeLocation(), true);
                    b.displayBreaksLeft(p.getEyeLocation(), -1);
                }
            }
            away.remove(_b);
        }

        playerVisibleBlocks.put(p, nearby);
    }

    @EventHandler
    public static void switchItem(PlayerItemHeldEvent event) { // if a player switches items, update nearby ReinforcedBlock(s)
        Player p = event.getPlayer();
        PlayerMoveEvent e = new PlayerMoveEvent(p, p.getLocation(), p.getLocation());
        playerMove(e);
    }

    @EventHandler
    public static void blockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        ReinforcedBlock b = ReinforcedBlock.GetAtLocation(event.getBlock().getLocation());
        if (b != null) {
            b.Break(p.getEyeLocation(), 1);
            if (b.GetBreaksLeft() > 0) {
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand.getItemMeta() instanceof Damageable) {
                    Damageable meta = (Damageable) hand.getItemMeta();
                    meta.setDamage(meta.getDamage() + 1);
                    hand.setItemMeta((ItemMeta) meta);
                }
                p.updateInventory();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onBlockExplode(BlockExplodeEvent event) {
        List<Block> affected = event.blockList();
        Location center = event.getBlock().getLocation();
        int maxDmg = (int)ConfigurationLoader.ConfigValues.get("reinforcement_explosion_strength");
        Iterator<Block> iter = affected.iterator();
        while(iter.hasNext()) {
            Block b = iter.next();
            if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) continue;
            ReinforcedBlock rb = ReinforcedBlock.GetAtLocation(b.getLocation());
            if (rb == null) continue;
            iter.remove();
            int calculatedDmg = (int)Math.floor(rb.GetLocation().distance(center));
            int damage = (int)Utils.clamp(maxDmg - calculatedDmg, 0, maxDmg);
            if (rb.GetBreaksLeft() - damage <= 0) {
                rb.GetLocation().getWorld().dropItemNaturally(rb.GetLocation(), new ItemStack(rb.GetLocation().getBlock().getType(), 1));
                rb.GetLocation().getBlock().setType(Material.AIR);
            }
            rb.Break(center, damage, 40);
        }
    }

    @EventHandler
    public static void onEntityExplode(EntityExplodeEvent event) {
        List<Block> affected = event.blockList();
        Location center = event.getLocation();
        int maxDmg = (int)ConfigurationLoader.ConfigValues.get("reinforcement_explosion_strength");
        Iterator<Block> iter = affected.iterator();
        while(iter.hasNext()) {
            Block b = iter.next();
            if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) continue;
            ReinforcedBlock rb = ReinforcedBlock.GetAtLocation(b.getLocation());
            if (rb == null) continue;
            iter.remove();
            int calculatedDmg = (int)Math.floor(rb.GetLocation().distance(center));
            int damage = (int)Utils.clamp(maxDmg - calculatedDmg, 0, maxDmg);
            if (rb.GetBreaksLeft() - damage <= 0) {
                rb.GetLocation().getWorld().dropItemNaturally(rb.GetLocation(), new ItemStack(rb.GetLocation().getBlock().getType(), 1));
                rb.GetLocation().getBlock().setType(Material.AIR);
            }
            rb.Break(center, damage, 40);
        }
    }

    @EventHandler
    public static void fireSpread(BlockIgniteEvent event) {
        Block to = event.getBlock();
        ReinforcedBlock rb = ReinforcedBlock.GetAtLocation(to.getLocation());
        boolean doFireSpread = (boolean)ConfigurationLoader.ConfigValues.get("reinforcement_blocks_can_burn");
        if (rb != null) {
            if (!doFireSpread) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public static void fireDamage(BlockBurnEvent event) {
        Block to = event.getBlock();
        ReinforcedBlock rb = ReinforcedBlock.GetAtLocation(to.getLocation());
        boolean doFireSpread = (boolean)ConfigurationLoader.ConfigValues.get("reinforcement_blocks_can_burn");
        if (rb != null) {
            event.setCancelled(true);
            if (!doFireSpread) {
                return;
            } else {
                rb.Break(rb.GetLocation(), 1, 40);
            }
        }  
    }
}