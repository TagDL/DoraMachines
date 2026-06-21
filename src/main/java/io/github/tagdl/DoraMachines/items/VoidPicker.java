package io.github.tagdl.DoraMachines.items;

import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.tagdl.DoraMachines.DoraMachines;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.window.Window;

public class VoidPicker extends RebarItem implements InteractRebarItemHandler{
    private static final NamespacedKey MODE_KEY = new NamespacedKey(DoraMachines.getInstance(), "void_picker_mode");
    private static final NamespacedKey INVENTORY_KEY = new NamespacedKey(DoraMachines.getInstance(), "void_picker_inventory");
    private static final NamespacedKey UUID_KEY = new NamespacedKey(DoraMachines.getInstance(), "void_picker_uuid");

    private final ItemStack WHITELIST = ItemStackBuilder.of(Material.WHITE_CONCRETE)
                .name(Component.translatable("doramachines.gui.void_picker.whitelist"))
                .lore(Component.translatable("doramachines.gui.void_picker.lore"))
                .build();
    private final ItemStack BLACKLIST = ItemStackBuilder.of(Material.BLACK_CONCRETE)
                .name(Component.translatable("doramachines.gui.void_picker.blacklist"))
                .lore(Component.translatable("doramachines.gui.void_picker.lore"))
                .build();
    private VirtualInventory modeInventory = new VirtualInventory(new ItemStack[]{getRawMode() ? BLACKLIST : WHITELIST});
    private VirtualInventory listInventory = getInventory();

    public VoidPicker(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }
        setUUID(UUID.randomUUID());
        Window.builder()
                .setUpperGui(make(event.getPlayer()))  
                .setTitle(Component.translatable("doramachines.gui.void_picker.name"))
                .open(event.getPlayer());
    }
    public Gui make(Player player) {
        Gui gui = Gui.builder()
            .setStructure("# # s m m m m # #")
            .addIngredient('#', GuiItems.background())
            .addIngredient('s', modeInventory)
            .addIngredient('m', listInventory)
            .build();
        modeInventory.addPreUpdateHandler(event -> event.setCancelled(!(event.getUpdateReason() instanceof MachineUpdateReason)));
        modeInventory.addClickHandler(event -> {
            if (!hasVoidPicker(player)) return;
            setMode(player, !getMode(player));
            event.getInventory().setItem(new MachineUpdateReason(), event.getSlot(), getMode(player) ? BLACKLIST : WHITELIST);
        });
        listInventory.addPreUpdateHandler(event -> {
            if (event.getUpdateReason() instanceof MachineUpdateReason) {
                event.setCancelled(!event.isRemove() && listInventory.containsSimilar(event.getNewItem()));
                return;
            }
            event.setCancelled(true);
            if ((RebarItem.fromStack(event.getNewItem()) instanceof VoidPicker) || !hasVoidPicker(player)) return;
            event.getInventory().setItem(new MachineUpdateReason(), event.getSlot(), 
                event.getNewItem() == null || event.getNewItem().isEmpty()
                    ? ItemStack.of(Material.AIR) : event.getNewItem().asOne());
        });
        listInventory.addPostUpdateHandler(event -> setInventory(player, listInventory));
        return gui;
    }
    public void setMode(Player player, boolean mode) { 
        findVoidPicker(player).editPersistentDataContainer(pdc -> pdc.set(MODE_KEY, RebarSerializers.BOOLEAN, mode));
    }
    public boolean getMode(Player player) { // false is whitelist else blacklist
        return findVoidPicker(player).getPersistentDataContainer().getOrDefault(MODE_KEY, RebarSerializers.BOOLEAN, false);
    }
    public boolean getRawMode() { // false is whitelist else blacklist
        return getStack().getPersistentDataContainer().getOrDefault(MODE_KEY, RebarSerializers.BOOLEAN, false);
    }
    public void setInventory(Player player, VirtualInventory inventory) { 
        findVoidPicker(player).editPersistentDataContainer(pdc -> pdc.set(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, inventory));
    }
    public VirtualInventory getInventory() {
        return getStack().getPersistentDataContainer()
            .getOrDefault(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, new VirtualInventory(4));
    }
    public void setUUID(UUID uuid) { 
        getStack().editPersistentDataContainer(pdc -> pdc.set(UUID_KEY, RebarSerializers.UUID, uuid));
    }
    public UUID getUUID() {
        return getStack().getPersistentDataContainer().getOrDefault(UUID_KEY, RebarSerializers.UUID, UUID.randomUUID());
    }
    public boolean hasVoidPicker(Player player) {
        Boolean hasVoidPicker = false;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (!(RebarItem.fromStack(itemStack) instanceof VoidPicker item)) continue;
            if (item.getUUID().equals(getUUID())) {
                hasVoidPicker = true;
                break;
            }
        }
        return hasVoidPicker;
    }
    public ItemStack findVoidPicker(Player player) {
        ItemStack openingItemStack = getStack();
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (!(RebarItem.fromStack(itemStack) instanceof VoidPicker item)) continue;
            if (item.getUUID().equals(getUUID())) openingItemStack = itemStack;
        }
        return openingItemStack;
    }
    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        Component component = Component.text("");
        for (ItemStack itemStack : getInventory().getItems()) {
            if (itemStack == null || itemStack.isEmpty()) continue;
            component = component.appendNewline()
                .append(Component.translatable("doramachines.item.void_picker.prefix"))
                .append(itemStack.effectiveName());
        }
        return List.of(RebarArgument.of("mode", getRawMode()
                ? Component.translatable("doramachines.gui.void_picker.blacklist")
                : Component.translatable("doramachines.gui.void_picker.whitelist")),
            RebarArgument.of("item", component));
    }
    public static class PickupListener implements Listener {
        @EventHandler
        public void onPlayerPickup(EntityPickupItemEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            ItemStack itemStack = event.getItem().getItemStack();
            for (ItemStack itemStack2 : player.getInventory().getContents()) {
                if (!(RebarItem.fromStack(itemStack2) instanceof VoidPicker picker)) continue;
                boolean hasSimilar = picker.getInventory().containsSimilar(itemStack);
                if (picker.getMode(player) ? hasSimilar : !hasSimilar) continue; //false is whitelist else blacklist
                event.setCancelled(true);
                itemStack.setAmount(0);
                player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, event.getItem().getLocation(), 1,
                            new Particle.DustTransition(Color.BLACK, Color.BLACK,2));
                event.getItem().setItemStack(itemStack);
                break;
            }
        }
    }
}
