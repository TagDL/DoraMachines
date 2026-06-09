package io.github.tagdl.DoraMachines.blocks;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;
import static java.lang.Math.max;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

public class TimingCharger extends RebarBlock implements
        DirectionalRebarBlock,
        ProcessorRebarBlock,
        GuiRebarBlock,
        TickingRebarBlock
{
    private static final NamespacedKey TIMING_KEY = pylonKey("amount");
    private static ConfigSection settings = ConfigSection.fromSettings(DoraMachinesKeys.TIMING_CHARGER);
    private static int click_per_change = settings.getOrThrow("Click_Per_Change", ConfigAdapter.INTEGER);
    private static int min_tick = max(settings.getOrThrow("Min_Tick", ConfigAdapter.INTEGER), 1);
    @Getter
    private int timing = 5;

    @SuppressWarnings("unused")
    public TimingCharger(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(timing);
        this.setFacing(context.getFacing());
    }
    @SuppressWarnings("unused")
    public TimingCharger(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        timing = pdc.get(TIMING_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(TIMING_KEY, RebarSerializers.INTEGER, timing);
    }
    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # m # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('m', new TimingItem())
                .build();
    }
    public class TimingItem extends AbstractItem {
        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player player) {
            return ItemStackBuilder.of(Material.RED_CONCRETE)
                    .name(Component.translatable("doramachines.gui.timing_charger.name")
                        .arguments(RebarArgument.of("amount", timing)))
                    .lore(Component.translatable("doramachines.gui.timing_charger.lore")
                        .arguments(RebarArgument.of("per", click_per_change),
                            RebarArgument.of("min", min_tick)));
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            if (clickType.isLeftClick()) {
                timing += click_per_change;
            } else if (clickType.isRightClick()) {
                timing -= click_per_change;
            }
            timing = max(timing, min_tick);
            notifyWindows();
        }
    }
    @Override
    public void tick() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        if (isProcessing()) {
            progressProcess(getTickInterval());
            return;            
        }
        startProcess(timing);
    }
    @Override
    public void onProcessFinished() {
        if (getBlock().getRelative(BlockFace.DOWN, 2).getType() == Material.AIR) return;
        if (getBlock().getRelative(BlockFace.DOWN, 2).getState() instanceof Dispenser dispenser) {
            dispenser.dispense();
        }
        if (getBlock().getRelative(BlockFace.DOWN, 2).getState() instanceof Dropper dropper) {
            dropper.drop();
        }
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player).add(
                Component.translatable("doramachines.waila.timing_charger.delay")
                    .arguments(RebarArgument.of("delay", timing)));
    }
}
