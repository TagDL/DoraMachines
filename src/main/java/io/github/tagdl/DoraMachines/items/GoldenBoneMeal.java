package io.github.tagdl.DoraMachines.items;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesItems;
import net.kyori.adventure.text.Component;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;
import org.bukkit.block.data.Directional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;

public class GoldenBoneMeal extends RebarItem implements
    RebarBlockInteractor,
    RebarDirectionalBlock,
    Listener
{
    public static final Component SUCCESS = Component.translatable("doramachines.message.unbreakable_result.success");
    public GoldenBoneMeal(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override @MultiHandler(priorities = EventPriority.LOWEST, ignoreCancelled = true)
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND 
                || event.useItemInHand() == Event.Result.DENY
                || !event.getAction().isRightClick()) return;
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        this.getKey();
        if (!grow(event, item, block)) return;

        item.subtract(); 
        event.setCancelled(true);
    }
    //for player
    public static boolean grow(@NotNull PlayerInteractEvent event, ItemStack item, Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable ageable)) return false;
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
                event.setCancelled(true);
                return false;
            }
            while (height < 3) {
                if (topCane.getRelative(BlockFace.UP).getType() != Material.AIR) break;
                    topCane.getRelative(BlockFace.UP).setType(Material.SUGAR_CANE);
                    topCane = topCane.getRelative(BlockFace.UP);
                    height++;
            }
            return true;
        }
        int currentAge = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        if (currentAge >= maxAge) {
            event.setCancelled(true);
            return false;
        } 

        ageable.setAge(maxAge);
        block.setBlockData(ageable);

        return true;
    }
    //for dispenser
    public static boolean grow(BlockDispenseEvent event, ItemStack item, Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable ageable)) return false;
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
                event.setCancelled(true);
                return false;
            }
            while (height < 3) {
                if (topCane.getRelative(BlockFace.UP).getType() != Material.AIR) break;
                    topCane.getRelative(BlockFace.UP).setType(Material.SUGAR_CANE);
                    topCane = topCane.getRelative(BlockFace.UP);
                    height++;
            }
            return true;
        }
        int currentAge = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        if (currentAge >= maxAge) {
            event.setCancelled(true);
            return false;
        } 

        ageable.setAge(maxAge);
        block.setBlockData(ageable);

        return true;
    }

    public static class GoldenBoneMealListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onDispense(BlockDispenseEvent event) {
            if (event.getItem().getType() != DoraMachinesItems.GOLDEN_BONE_MEAL.getType()) return;
            if (BlockStorage.get(event.getBlock()) == null
                    && event.getBlock().getType() == Material.DISPENSER) {
                    if (!(event.getBlock().getBlockData() instanceof Directional directional)) return;

                    Block crop = event.getBlock().getRelative(directional.getFacing());
                    ItemStack item = event.getItem();
                    if (!grow(event, item, crop)) return;
                    event.setCancelled(true);
                    // event.setItem(new ItemStack(Material.STONE));
                    Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                        Dispenser dispenser = (Dispenser) event.getBlock().getState(false);
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

