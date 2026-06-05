package io.github.tagdl.DoraMachines.blocks.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;

import java.util.List;
import java.util.function.BiConsumer;
public class MultiFluidSelector{
    public static PagedGui<Inventory> make(@NotNull BiConsumer<RebarFluid, Player> setFluid){
        VirtualInventory inv = new VirtualInventory(RebarRegistry.FLUIDS.getValues().size());
        for (RebarFluid itemFluid : RebarRegistry.FLUIDS.getValues()) {
            inv.addItem(new MachineUpdateReason(), itemFluid.getItem());
        }
        inv.addClickHandler(event -> {
            RebarFluid fluid = RebarFluid.Companion.fromStack(event.getInventory().getItem(event.getSlot()));
            if (fluid != null) setFluid.accept(fluid, event.getPlayer());
        });
        inv.addPreUpdateHandler(event -> event.setCancelled(!(event.getUpdateReason() instanceof MachineUpdateReason)));
        BoundItem back = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                    .setName(Component.translatable("doramachines.gui.fluid_selector.backpage")))
                .addClickHandler((item, inner_gui, click) -> inner_gui.setPage(inner_gui.getPage() - 1))
                .build();
            BoundItem forward = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                    .setName(Component.translatable("doramachines.gui.fluid_selector.forwardpage")))
                .addClickHandler((item, inner_gui, click) -> inner_gui.setPage(inner_gui.getPage() + 1))
                .build();
        PagedGui<Inventory> gui = PagedGui.inventoriesBuilder()
                    .setStructure(
                            "U X X X X X X X X",
                            "A X X X X X X X X",
                            "D X X X X X X X X"
                    )
                    .addIngredient('A', GuiItems.background())
                    .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('U', back)
                    .setContent(List.of(inv))
                    .addIngredient('D', forward)
                    .build();
        return gui;
    }
}
