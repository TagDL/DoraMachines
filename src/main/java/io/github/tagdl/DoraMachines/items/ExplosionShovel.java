package io.github.tagdl.DoraMachines.items;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.BlockBreakRebarItemHandler;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import net.kyori.adventure.text.Component;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class ExplosionShovel extends RebarItem implements 
    InteractRebarItemHandler,
    BlockBreakRebarItemHandler    
{
    public ExplosionShovel(@NotNull ItemStack stack) {
        super(stack);
    }
    private static final NamespacedKey RADIUS_KEY_SHOVEL = new NamespacedKey(DoraMachines.getInstance(), "explosion_shovel_radius");
    private static final Set<Event> eventsToIgnore = Collections.newSetFromMap(new WeakHashMap<>());
    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreakBlock(@NotNull BlockBreakEvent event, @NotNull EventPriority priority) {
        if (BlockStorage.isRebarBlock(event.getBlock()) || eventsToIgnore.contains(event) || event.getPlayer().isSneaking()) {
            eventsToIgnore.remove(event);
            return;
        }
        breakArea(event.getBlock(), event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
    }
    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority){
        if (event.getHand() != EquipmentSlot.HAND 
                || event.useItemInHand() == Event.Result.DENY
                || !event.getAction().isRightClick()
                || !event.getPlayer().isSneaking()) return;
        int radius = event.getItem().getPersistentDataContainer()
            .getOrDefault(RADIUS_KEY_SHOVEL, PersistentDataType.INTEGER, 2);
        int next_radius = switch (radius) {
                case 2 -> 0;
                case 0 -> 1;
                default -> 2;
            };
        event.getItem().editPersistentDataContainer(pdc ->
            pdc.set(RADIUS_KEY_SHOVEL, PersistentDataType.INTEGER, next_radius));
        event.getPlayer().sendActionBar(Component
                .translatable("doramachines.message.explosion.success")
                .arguments(RebarArgument.of("radius", (next_radius * 2) + 1)));
    }

    private void breakArea(Block centerBlock, Player player, ItemStack tool) {
        int radius = tool.getPersistentDataContainer()
            .getOrDefault(RADIUS_KEY_SHOVEL, PersistentDataType.INTEGER, 2);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block target = centerBlock.getRelative(x, y, z);
                    if (target.getType().isAir() || target.getType().getHardness() < 0 
                            || BlockStorage.isRebarBlock(target)) continue;
                    BlockBreakEvent breakEvent = new BlockBreakEvent(target, player);
                    eventsToIgnore.add(breakEvent);
                    if (!breakEvent.callEvent()) continue;
                    startDestory(target, player, tool, breakEvent);
                }
            }
        }
    }

    private void startDestory(Block block, Player player, ItemStack tool, BlockBreakEvent event) {
        BlockState blockState = block.getState();
        Collection<ItemStack> drops = block.getDrops(tool);
        if (blockState instanceof Container container && !(blockState instanceof ShulkerBox)) {
            Inventory inv = container.getInventory();
            for (ItemStack item : inv.getContents()) {
                if (item != null && !item.getType().isAir()) drops.add(item);
            }
        }
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
        block.setType(Material.AIR);
        if (event.isDropItems()) {
            List<Item> itemsDropped = new ArrayList<>();
            for (ItemStack itemStack : drops) {
                Item item = block.getWorld().dropItem(block.getLocation(), itemStack);
                itemsDropped.add(item);
            }
            if (!new BlockDropItemEvent(block, blockState, player, itemsDropped).callEvent()) itemsDropped.forEach(Item::remove);
        }
        Tool toolComponent = tool.getData(DataComponentTypes.TOOL);
        if (toolComponent != null) RebarUtils.damageItem(tool, toolComponent.damagePerBlock(), player, EquipmentSlot.HAND);
    }
}
