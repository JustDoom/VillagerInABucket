package com.imjustdoom.villagerinabucket.folia.listener;

import com.imjustdoom.villagerinabucket.folia.VillagerInABucketPlugin;
import com.imjustdoom.villagerinabucket.folia.util.FoliaUtil;
import com.imjustdoom.villagerinabucket.folia.util.VillagerDataUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class VillagerInteractListener implements Listener {

    private final VillagerInABucketPlugin plugin;

    public VillagerInteractListener(VillagerInABucketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity entity = event.getRightClicked();
        if (!isPickupable(entity)) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType() != Material.BUCKET) return;

        event.setCancelled(true);
        playPickupSound(entity);

        if (plugin.isHarmReputation() && entity instanceof Villager villager) {
            villager.damage(0.01, player);
        }

        ItemStack villagerBucket = VillagerDataUtil.createVillagerBucket(entity);
        entity.remove();

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
            if (!player.getInventory().addItem(villagerBucket).isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), villagerBucket);
            }
        } else {
            player.getInventory().setItemInMainHand(villagerBucket);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack hand = event.getItem();
        if (!VillagerDataUtil.isVillagerBucket(hand)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        BlockFace face = event.getBlockFace();
        Location spawnLoc = clickedBlock.getRelative(face).getLocation().add(0.5, 0, 0.5);

        FoliaUtil.runAtLocation(plugin, spawnLoc, () -> {
            Entity spawned = VillagerDataUtil.spawnFromBucket(hand, spawnLoc);
            if (spawned != null) {
                spawnLoc.getWorld().playSound(spawnLoc, getPlaceSound(spawned), 1.0f, 1.0f);
            }
        });

        player.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();

        if (VillagerDataUtil.isVillagerBucket(item)) {
            event.setCancelled(true);

            Block dispenser = event.getBlock();
            org.bukkit.block.data.type.Dispenser dispenserData = (org.bukkit.block.data.type.Dispenser) dispenser.getBlockData();
            BlockFace facing = dispenserData.getFacing();
            Location spawnLoc = dispenser.getRelative(facing).getLocation().add(0.5, 0, 0.5);

            FoliaUtil.runAtLocation(plugin, spawnLoc, () -> {
                Entity spawned = VillagerDataUtil.spawnFromBucket(item, spawnLoc);
                if (spawned != null) {
                    spawnLoc.getWorld().playSound(spawnLoc, getPlaceSound(spawned), 1.0f, 1.0f);
                }

                org.bukkit.block.Dispenser dispenserBlock = (org.bukkit.block.Dispenser) dispenser.getState();
                org.bukkit.inventory.Inventory inv = dispenserBlock.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack slot = inv.getItem(i);
                    if (VillagerDataUtil.isVillagerBucket(slot)) {
                        inv.setItem(i, new ItemStack(Material.BUCKET));
                        break;
                    }
                }
            });
        } else if (item.getType() == Material.BUCKET) {
            Block dispenser = event.getBlock();
            org.bukkit.block.data.type.Dispenser dispenserData = (org.bukkit.block.data.type.Dispenser) dispenser.getBlockData();
            BlockFace facing = dispenserData.getFacing();
            Block targetBlock = dispenser.getRelative(facing);

            FoliaUtil.runAtLocation(plugin, targetBlock.getLocation(), () -> {
                for (Entity entity : targetBlock.getWorld().getNearbyEntities(
                        targetBlock.getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)) {
                    if (!isPickupable(entity)) continue;

                    ItemStack villagerBucket = VillagerDataUtil.createVillagerBucket(entity);
                    entity.remove();

                    org.bukkit.block.Dispenser dispenserBlock = (org.bukkit.block.Dispenser) dispenser.getState();
                    org.bukkit.inventory.Inventory inv = dispenserBlock.getInventory();
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack slot = inv.getItem(i);
                        if (slot != null && slot.getType() == Material.BUCKET && !VillagerDataUtil.isVillagerBucket(slot)) {
                            if (slot.getAmount() > 1) {
                                slot.setAmount(slot.getAmount() - 1);
                                inv.setItem(i, slot);
                                if (!inv.addItem(villagerBucket).isEmpty()) {
                                    dispenser.getWorld().dropItemNaturally(
                                            targetBlock.getLocation().add(0.5, 0.5, 0.5), villagerBucket);
                                }
                            } else {
                                inv.setItem(i, villagerBucket);
                            }
                            break;
                        }
                    }
                    break;
                }
            });

            event.setCancelled(true);
        }
    }

    private boolean isPickupable(Entity entity) {
        if (entity instanceof Villager) return true;
        if (entity instanceof WanderingTrader) return true;
        if (entity instanceof ZombieVillager) return plugin.isEnableZombieVillager();
        return false;
    }

    private void playPickupSound(Entity entity) {
        Sound sound;
        if (entity instanceof Villager) {
            sound = Sound.ENTITY_VILLAGER_TRADE;
        } else if (entity instanceof WanderingTrader) {
            sound = Sound.ENTITY_WANDERING_TRADER_NO;
        } else if (entity instanceof ZombieVillager) {
            sound = Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
        } else {
            return;
        }
        entity.getWorld().playSound(entity.getLocation(), sound, 1.0f, 1.0f);
    }

    private Sound getPlaceSound(Entity entity) {
        if (entity instanceof Villager) return Sound.ENTITY_VILLAGER_AMBIENT;
        if (entity instanceof WanderingTrader) return Sound.ENTITY_WANDERING_TRADER_AMBIENT;
        if (entity instanceof ZombieVillager) return Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
        return Sound.ENTITY_VILLAGER_AMBIENT;
    }
}
