package io.github.tagdl.DoraMachines.items;

import static io.github.pylonmc.pylon.util.PylonUtils.colorToTextColor;

import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.blocks.RedstoneLink;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;

public class LinkedConnector extends RebarItem implements RebarBlockInteractor {
    private static final NamespacedKey STORED_UUID_KEY = new NamespacedKey(DoraMachines.getInstance(), "stored_uuid_key");
    public LinkedConnector(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND 
                || !event.getAction().isRightClick()) return;
        if (!(BlockStorage.get(event.getClickedBlock()) instanceof RedstoneLink redstoneLink)) return;
        if (event.getPlayer().isSneaking()) { //show what link
            List<RedstoneLink> blocks = redstoneLink.findBlocks(!redstoneLink.getLinkType());
            if (blocks.isEmpty()) {
                event.getPlayer().sendMessage(Component.translatable("doramachines.message.linked_connector.not_found"));
                return;
            }
            blocks.forEach(linker -> {
                event.getPlayer().sendMessage((!redstoneLink.getLinkType()
                            ? Component.translatable("doramachines.message.linked_connector.find_receiver")
                            : Component.translatable("doramachines.message.linked_connector.find_transmitter"))
                        .append(Component.text(" " + getLocationString(linker.getBlock().getLocation()))
                            .color(colorToTextColor(Color.LIME))));
            });
            return;
        }
        if (!redstoneLink.getLinkType()) { //sender logic
            setStoredUuid(redstoneLink.getUUID());
            getStack().editMeta(meta -> meta.itemName(Component.text(getLocationString(redstoneLink.getBlock().getLocation()))));
            getStack().setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            event.getPlayer().sendMessage(Component.translatable("doramachines.message.linked_connector.record_success"));
        } else { //receiver logic
            if (getStoredUuid() == null) event.getPlayer()
                .sendMessage(Component.translatable("doramachines.message.linked_connector.set_failure"));
            else {
                redstoneLink.setUUID(getStoredUuid());
                event.getPlayer().sendMessage(Component.translatable("doramachines.message.linked_connector.set_success"));
            }
        }
    }
    private String getLocationString(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
    public UUID getStoredUuid() {
        return getStack().getPersistentDataContainer().getOrDefault(STORED_UUID_KEY, RebarSerializers.UUID, null);
    }
    public void setStoredUuid(UUID uuid) {
        getStack().editPersistentDataContainer(pdc -> pdc.set(STORED_UUID_KEY, RebarSerializers.UUID, uuid));
    }
}
