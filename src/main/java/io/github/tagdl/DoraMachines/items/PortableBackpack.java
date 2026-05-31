package io.github.tagdl.DoraMachines.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.tagdl.DoraMachines.DoraMachines;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

public class PortableBackpack extends RebarItem implements
    RebarInteractor
{
    private static final NamespacedKey INVENTORY_KEY = new NamespacedKey(DoraMachines.getInstance(), "backpack_inventory");
    private static final NamespacedKey NAME_KEY = new NamespacedKey(DoraMachines.getInstance(), "backpack_name");
    public PortableBackpack(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority){
        if (event.getHand() != EquipmentSlot.HAND 
                || event.useItemInHand() == Event.Result.DENY
                || event.getAction().isLeftClick()) return;
        
        PagedGui<Inventory> gui = make();
        open(gui, event.getPlayer());
    }
    public void open(Gui gui, Player player) {
        Window.builder()
            .setUpperGui(gui)  
            .setTitle(getName())  
            .open(player);
    }
    public void setName(Player player, Component name) {
        getStack().editMeta(meta -> meta.itemName(name));
        getStack().editPersistentDataContainer(pdc -> pdc.set(NAME_KEY,
                RebarSerializers.COMPONENT, Component.text(player.getUniqueId().toString() + "|").append(name)));
    }
    public Component getName() {
        return getStack().getPersistentDataContainer()
            .getOrDefault(NAME_KEY, RebarSerializers.COMPONENT, Component.translatable("doramachines.item.portable_backpack.name"));
    }
    public VirtualInventory getInventory() {
        return getStack().getPersistentDataContainer()
            .getOrDefault(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, new VirtualInventory(7 * 18));
    }
    public PagedGui<Inventory> make() {
        VirtualInventory inv = getInventory();
        BoundItem back = BoundItem.pagedBuilder()
            .setItemProvider(new ItemBuilder(Material.ARROW))
            .addClickHandler((item, inner_gui, click) -> inner_gui.setPage(inner_gui.getPage() - 1))
            .build();
        BoundItem forward = BoundItem.pagedBuilder()
            .setItemProvider(new ItemBuilder(Material.ARROW))
            .addClickHandler((item, inner_gui, click) -> inner_gui.setPage(inner_gui.getPage() + 1))
            .build();
        BoundItem craft = BoundItem.pagedBuilder()
            .setItemProvider(new ItemBuilder(Material.CRAFTING_TABLE))
            .addClickHandler((item, inner_gui, click) -> 
                inner_gui.getCurrentViewers().forEach(player->player.openInventory(MenuType.CRAFTING.create(player))))
            .build();
        PagedGui<Inventory> gui = PagedGui.inventoriesBuilder()
                .setStructure(
                        "U X X X X X X X A",
                        "C X X X X X X X A",
                        "A X X X X X X X A",
                        "A X X X X X X X A",
                        "A X X X X X X X A",
                        "D X X X X X X X A"
                )
                .addIngredient('A', GuiItems.background())
                .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('C', craft)
                .setContent(List.of(inv))
                .addIngredient('U', back)
                .addIngredient('D', forward)
                .build();
        inv.addPostUpdateHandler(event -> {
           getStack().editPersistentDataContainer(pdc ->
                pdc.set(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, inv));
        });
        return gui;
    }

}
