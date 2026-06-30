package io.github.tagdl.DoraMachines.items;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.tagdl.DoraMachines.DoraMachines;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public class RoadTool extends RebarItem implements
    InteractRebarItemHandler,
    ProcessorRebarBlock
{
    @Getter
    private final int deleteDelay = getSettings().getOrThrow("delete_delay_seconds", ConfigAdapter.INTEGER) * 20;
    private static final NamespacedKey ENABLED_KEY = PylonUtils.pylonKey("road_tool_toggler");
    private static final NamespacedKey BLOCK_KEY = PylonUtils.pylonKey("road_tool_block");
    public RoadTool(@NotNull ItemStack stack) {
        super(stack);
    }
    @SuppressWarnings("UnstableApiUsage")
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            Player player = event.getPlayer();
            getStack().editPersistentDataContainer(pdc -> {
                boolean enabled = !isEnabled();
                player.sendMessage(Component.translatable("doramachines.message.road_tool." +
                        (enabled ? "enabled" : "disabled")));
                pdc.set(ENABLED_KEY, PersistentDataType.BOOLEAN, enabled);
            });
        } else {
            Player player = event.getPlayer();
            if (event.getClickedBlock() == null) return;
            if (event.getClickedBlock().getType() == Material.AIR) return;
            Block block = event.getClickedBlock();
            getStack().editPersistentDataContainer(pdc -> {
                pdc.set(BLOCK_KEY, RebarSerializers.NAMESPACED_KEY, block.getType().getKey());
                player.sendMessage(Component.translatable("doramachines.message.road_tool.block_set")
                        .arguments(RebarArgument.of("block", ItemStack.of(block.getType()).effectiveName())));
            });
        }
    }
    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("enabled",
                Component.translatable("doramachines.item.road_tool." + (isEnabled() ? "enabled" : "disabled"))),
            RebarArgument.of("delete_delay", deleteDelay / 20),
            RebarArgument.of("block", getRoadBlock().effectiveName()));
    }

    public ItemStack getRoadBlock() {
        NamespacedKey blockKey = getStack().getPersistentDataContainer().getOrDefault(BLOCK_KEY, RebarSerializers.NAMESPACED_KEY, Material.STONE.getKey());
        return PylonUtils.itemFromKey(blockKey);
    }
    public boolean isEnabled() {
        return getStack().getPersistentDataContainer().getOrDefault(ENABLED_KEY, PersistentDataType.BOOLEAN, false);
    }
    public void onRemoveRoadBlock(Block block, Player player, Material material) {
        Bukkit.getScheduler().runTaskLater(DoraMachines.getInstance(), () -> {
            if (!block.getChunk().isLoaded()) return;
            if (!player.isOnline() || player.isDead()) return;
            if (!player.getInventory().contains(getRoadBlock().getType()) 
                    && player.getInventory().firstEmpty() == -1) return;
            if (block.getType() == material) {
                block.setType(Material.AIR);
                player.give(getRoadBlock().asOne());
            }
        }, deleteDelay);
    }
    public boolean canPlace(Block block, BlockState data, Player player) { 
        return new BlockPlaceEvent(block, data, null,
                    ItemStack.of(Material.STONE), player, true, EquipmentSlot.HAND).callEvent();
    }
    public static final class PlayMoving implements Listener {
        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event){
            Player player = event.getPlayer();
            RoadTool roadTool = null;
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (RebarItem.fromStack(itemStack) instanceof RoadTool roadtool) {
                    roadTool = roadtool;
                    break;
                }
            }
            if (roadTool == null) return;
            if (!roadTool.isEnabled()) return;
            if (!player.isOnline()) return;
            if (!player.isSneaking() || player.isDead() || player.isFlying()
                    || player.getGameMode() == GameMode.SPECTATOR
                    || player.getGameMode() == GameMode.ADVENTURE) return;
            if (!player.getInventory().contains(roadTool.getRoadBlock().getType())) return;
            Location loc = player.getLocation().toCenterLocation().add(0, -1, 0);
            if (!roadTool.canPlace(loc.getBlock(), loc.getBlock().getState(), player)) return;
            if (loc.getBlock().isEmpty()) {
                Block block = loc.getBlock();
                Material material = roadTool.getRoadBlock().getType();
                block.setType(material);
                
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack != null && itemStack.isSimilar(roadTool.getRoadBlock())) {
                        Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                            if (itemStack.getAmount() > 1) {
                                itemStack.subtract();
                            } else {
                                player.getInventory().remove(itemStack);
                            }
                        });
                        break;
                    }
                }
                roadTool.onRemoveRoadBlock(block, player, material);
            }
        }
    }
}
