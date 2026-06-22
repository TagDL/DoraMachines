package io.github.tagdl.DoraMachines.items;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.destroystokyo.paper.ParticleBuilder;

import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.BlockInteractRebarItemHandler;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;

public class GoldenBoneMeal extends RebarItem implements
    BlockInteractRebarItemHandler,
    DirectionalRebarBlock,
    Listener
{
    public GoldenBoneMeal(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override @MultiHandler(priorities = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractWithBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND 
                || event.useItemInHand() == Event.Result.DENY
                || !event.getAction().isRightClick()) return;
        if (!grow(event.getClickedBlock())) return;
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.getItem().subtract(); 
    }
    public static boolean grow(Block block) {
        BlockData data = block.getBlockData();
        if (BlockStorage.isRebarBlock(block)) return false;
        if (data instanceof Sapling) {
            growTree(block);
            return true;
        }
        if (!(data instanceof Ageable ageable)) {
            if (block.getType() == Material.GRASS_BLOCK) {
                block.applyBoneMeal(BlockFace.UP);
                return true;
            }
            return false;
        }
        if (block.getType() == Material.SUGAR_CANE) {

            Block downCane = block;
            while (downCane.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
                downCane = downCane.getRelative(BlockFace.DOWN);
            }
            Block topCane = downCane;
            int height = 1;
            while (topCane.getRelative(BlockFace.UP).getType() == Material.SUGAR_CANE) {
                topCane = topCane.getRelative(BlockFace.UP);
                height++;
            }
            
            if (height >= 3) {
                return false;
            }
            while (height < 3) {
                if (topCane.getRelative(BlockFace.UP).getType() != Material.AIR) break;
                    topCane.getRelative(BlockFace.UP).setType(Material.SUGAR_CANE);
                    topCane = topCane.getRelative(BlockFace.UP);
                    height++;
            }
            new ParticleBuilder(Particle.HAPPY_VILLAGER).location(block.getLocation()).spawn();
            return true;
        }
        int currentAge = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        if (currentAge >= maxAge) return false;

        ageable.setAge(maxAge);
        block.setBlockData(ageable);
        new ParticleBuilder(Particle.HAPPY_VILLAGER).location(block.getLocation()).spawn();
        return true;
    }
    private static void growTree(Block block) {
        if (block.getBlockData() instanceof Sapling) {
            Block freshBlock = block.getWorld().getBlockAt(block.getLocation());
            freshBlock.applyBoneMeal(BlockFace.UP);
            Bukkit.getScheduler().runTask(DoraMachines.getInstance(),() -> growTree(freshBlock));
        }
    }
    public static class GoldenBoneMealListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onDispense(BlockDispenseEvent event) {
            ItemStack item = event.getItem();
            Block block = event.getBlock();
            if (!(RebarItem.fromStack(item) instanceof GoldenBoneMeal)) return;
            if (!BlockStorage.isRebarBlock(block)
                    && block.getState(false) instanceof Dispenser dispenser) {
                if (!(event.getBlock().getBlockData() instanceof Directional directional)) return;

                Block crop = event.getBlock().getRelative(directional.getFacing());
                if (!grow(crop)) return;
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                    Inventory inv = dispenser.getInventory();
                    ItemStack[] contents = inv.getContents();
                    for (int i = 0; i < contents.length; i++) {
                        ItemStack is = contents[i];
                        if (is != null && is.isSimilar(item)) {
                            int amount = is.getAmount();
                            if (amount >= 1) {
                                is.setAmount(amount - 1);
                                inv.setItem(i, is);
                            }
                            break;
                        }
                    }
                    dispenser.update();
                });

            }
        }
    }
}

