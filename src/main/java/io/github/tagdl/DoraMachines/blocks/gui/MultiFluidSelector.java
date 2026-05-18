package io.github.tagdl.DoraMachines.blocks.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;

import java.util.function.BiConsumer;
public class MultiFluidSelector{
    public static Gui make(@NotNull BiConsumer<RebarFluid, Player> setFluid){
        Gui gui = Gui.builder()
                .setStructure(
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . ."
                )
                .build();
        
        int i = 0;
        for (RebarFluid itemFluid : RebarRegistry.FLUIDS.getValues()) {
            gui.setItem(i, new FluidItem(itemFluid, setFluid));
            i++;
        }
        return gui;
    }
    private static class FluidItem extends AbstractItem {
        private final RebarFluid itemFluid;
        private final @NotNull BiConsumer<RebarFluid, Player> setFluid;

        public FluidItem(RebarFluid itemFluid, @NotNull BiConsumer<RebarFluid, Player> setFluid) {
            super();
            this.itemFluid = itemFluid;
            this.setFluid = setFluid;
        }

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            return ItemStackBuilder.of(itemFluid.getItem())
                    .name(Component.translatable("pylon.fluid." + itemFluid.getKey().getKey()));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            setFluid.accept(itemFluid, player);
        }
    }
}
