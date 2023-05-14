package codes.zucker.reinforcement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import codes.zucker.reinforcement.entity.ReinforcedBlock;
import codes.zucker.reinforcement.util.ConfigurationYaml;
import codes.zucker.reinforcement.util.Utils;

public class Events implements Listener {

    @EventHandler
    public static void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player p = event.getPlayer();
        
        ReinforcedBlock reinforcedAtTarget = ReinforcedBlock.getAtLocation(event.getClickedBlock().getLocation());
        

        if (!Commands.reToggle.contains(p)) {
            if (p.getGameMode() == GameMode.CREATIVE && reinforcedAtTarget != null)
                reinforcedAtTarget.destroyBlock(false);
            return;
        }
        
        PlayerInventory inventory = p.getInventory();
        Material hand = inventory.getItemInMainHand().getType();
        Block b = event.getClickedBlock();
        
        ReinforceMaterial reinforceMaterial = null;
        
        for(ReinforceMaterial mat : ReinforceMaterial.entries) {
            if (hand.equals(mat.getMaterial())) {
                reinforceMaterial = mat;
            }
        }

        if (reinforceMaterial == null) return;

        if (reinforcedAtTarget != null) {
            if (reinforcedAtTarget.getBreaksLeft() >= reinforceMaterial.getMaxAllowedBreaks()) {
                event.setCancelled(true);
                return;
            }
            if (!reinforceMaterial.getMaterial().equals(reinforcedAtTarget.getMaterialUsed().getMaterial()))
                return;
        }

        // quick check to disallow reinforcing all blacklisted blocks in the config
        String materialName = event.getClickedBlock().getType().toString();
        ConfigurationYaml.getList("reinforcement_not_reinforceable").forEach(s -> {
            String item = (String)s;
            if (materialName.contains(item) && !materialName.contains("BLOCK"))
                return;
        });

        Utils.removeFromHand(inventory, hand, 1);

        Random r = new Random();
        DustOptions particle = new DustOptions(Color.GRAY, 0.5f);
        for (int j = 0; j < r.nextInt(10) + 30; j++) { // fancy particles
            Location l = b.getLocation().clone();
            l.add(r.nextDouble() * 2f - 0.5f, r.nextDouble() * 2f - 0.5f, r.nextDouble() * 2f - 0.5f);
            p.getWorld().spawnParticle(Particle.REDSTONE, l, 5, particle);
        }

        ReinforcedBlock reinforced = reinforcedAtTarget != null ? reinforcedAtTarget : new ReinforcedBlock(b.getLocation(), 0, reinforceMaterial, p.getUniqueId());
        reinforced.reinforceBlock(reinforceMaterial.getBreaksPerReinforce());
        reinforced.displayNearest(p.getLocation());

        event.setCancelled(true);
    }

    static Set<ReinforcedBlock> visibleBlocks = new HashSet<>();

    @EventHandler
    public static void playerMove(PlayerMoveEvent event) {
        Set<ReinforcedBlock> visibleBlocksClone = new HashSet<>(visibleBlocks);
        visibleBlocksClone.forEach(b -> {
            if (b.getLocation() != null && Utils.getPlayersNear(b.getLocation(), 5).isEmpty()) {
                b.getHologram().hideHologramDelayed();
                visibleBlocks.remove(b);
            }
        });

        Player p = event.getPlayer();
        if (!Commands.rvToggle.contains(p)) return;
        Utils.getNearbyBlocks(p.getLocation(), 5).forEach(b -> {
            ReinforcedBlock reinforced = ReinforcedBlock.getAtLocation(b.getLocation());
            if (reinforced != null && 
                ((ConfigurationYaml.getBoolean("reinforcement_visibility_only_show_to_owner") && reinforced.getOwner().equals(p.getUniqueId()))
                || (!ConfigurationYaml.getBoolean("reinforcement_visibility_only_show_to_owner")))) {
                reinforced.displayNearest(p.getLocation(), false);
                visibleBlocks.add(reinforced);
            }
        });
        
    }

    @EventHandler
    public static void blockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        ReinforcedBlock b = ReinforcedBlock.getAtLocation(event.getBlock().getLocation());
        if (b != null) {
            b.damageBlock(p.getEyeLocation(), 1);
            if (b.getBreaksLeft() >= 0) {
                boolean damageOnlyOnLastBreak = ConfigurationYaml.getBoolean("reinforcement_damage_on_break_only");
                
                if (!damageOnlyOnLastBreak || b.getBreaksLeft() == 0) {
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    if (hand.getItemMeta() instanceof Damageable) {
                        Damageable meta = (Damageable) hand.getItemMeta();
                        meta.setDamage(meta.getDamage() + 1);
                        hand.setItemMeta(meta);
                    }
                    p.updateInventory();
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onBlockExplode(BlockExplodeEvent event) {
        List<Block> affected = event.blockList();
        Location center = event.getBlock().getLocation();
        int maxDmg = ConfigurationYaml.getInt("reinforcement_explosion_strength");
        Iterator<Block> iter = affected.iterator();
        while(iter.hasNext()) {
            Block b = iter.next();
            ReinforcedBlock rb = ReinforcedBlock.getAtLocation(b.getLocation());
            if (rb == null || b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) continue;
            iter.remove();
            int calculatedDmg = (int)Math.floor(rb.getLocation().distance(center));
            int damage = (int)Utils.clamp(maxDmg - calculatedDmg, 0, maxDmg);
            if (rb.getBreaksLeft() - damage <= 0) {
                rb.getLocation().getWorld().dropItemNaturally(rb.getLocation(), new ItemStack(rb.getLocation().getBlock().getType(), 1));
                rb.getLocation().getBlock().setType(Material.AIR);
            }
            rb.damageBlock(center, damage);
        }
    }

    @EventHandler
    public static void onEntityExplode(EntityExplodeEvent event) {
        List<Block> affected = event.blockList();
        Location center = event.getLocation();
        int maxDmg = ConfigurationYaml.getInt("reinforcement_explosion_strength");
        Iterator<Block> iter = affected.iterator();
        while(iter.hasNext()) {
            Block b = iter.next();
            ReinforcedBlock rb = ReinforcedBlock.getAtLocation(b.getLocation());
            if (rb == null || b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) continue;
            iter.remove();
            int calculatedDmg = (int)Math.floor(rb.getLocation().distance(center));
            int damage = (int)Utils.clamp(maxDmg - calculatedDmg, 0, maxDmg);
            if (rb.getBreaksLeft() - damage <= 0)
                b.breakNaturally();
            rb.damageBlock(center, damage);
        }
    }

    @EventHandler
    public static void fireSpread(BlockIgniteEvent event) {
        Block to = event.getBlock();
        ReinforcedBlock rb = ReinforcedBlock.getAtLocation(to.getLocation());
        boolean doFireSpread = ConfigurationYaml.getBoolean("reinforcement_blocks_can_burn");
        if (rb != null && !doFireSpread) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void fireDamage(BlockBurnEvent event) {
        Block to = event.getBlock();
        ReinforcedBlock rb = ReinforcedBlock.getAtLocation(to.getLocation());
        boolean doFireSpread = ConfigurationYaml.getBoolean("reinforcement_blocks_can_burn");
        if (rb != null) {
            event.setCancelled(true);
            if (doFireSpread)
                rb.damageBlock(rb.getLocation(), 1);
        }  
    }
}
