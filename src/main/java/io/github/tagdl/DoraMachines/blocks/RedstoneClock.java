package io.github.tagdl.DoraMachines.blocks;

import static java.lang.Math.max;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

public class RedstoneClock extends RebarBlock implements
        DirectionalRebarBlock,
        ProcessorRebarBlock,
        GuiRebarBlock,
        TickingRebarBlock,
        EntityHolderRebarBlock
{
    private static final NamespacedKey TIMING_KEY = new NamespacedKey(DoraMachines.getInstance(), "delay");
    private final ConfigSection settings = ConfigSection.fromSettings(DoraMachinesKeys.REDSTONE_CLOCK);
    private int click_per_change = settings.getOrThrow("Click_Per_Change", ConfigAdapter.INTEGER);
    private int min_tick = max(settings.getOrThrow("Min_Tick", ConfigAdapter.INTEGER), 1);
    @Getter
    private int timing = 5;

    @SuppressWarnings("unused")
    public RedstoneClock(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(1);
        this.setFacing(context.getFacing());
        addEntity("lamp", new BlockDisplayBuilder()
                .blockData(Bukkit.createBlockData("minecraft:redstone_lamp[lit=false]"))
                .transformation(new TransformBuilder().scale(1.01))
                .build(getBlock().getLocation().toCenterLocation()));
    }
    @SuppressWarnings("unused")
    public RedstoneClock(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
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
            return ItemStackBuilder.of(Material.CLOCK)
                    .name(Component.translatable("doramachines.gui.redstone_clock.name")
                        .arguments(RebarArgument.of("amount", timing)))
                    .lore(Component.translatable("doramachines.gui.redstone_clock.lore")
                        .arguments(RebarArgument.of("per", click_per_change),
                            RebarArgument.of("min", min_tick)));
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            if (clickType.isLeftClick()) timing += click_per_change;
            else if (clickType.isRightClick()) timing -= click_per_change;
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
        getBlock().setType(Material.RED_CONCRETE);
        getHeldEntity(BlockDisplay.class, "lamp")
            .setBlock(Bukkit.createBlockData("minecraft:redstone_lamp[lit=false]"));
        startProcess(timing);
    }
    @Override
    public void onProcessFinished() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        getHeldEntity(BlockDisplay.class, "lamp")
            .setBlock(Bukkit.createBlockData("minecraft:redstone_lamp[lit=true]"));
        getBlock().setType(Material.REDSTONE_BLOCK);
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display =  WailaDisplay.of(this, player);
        display.add(Component.translatable("doramachines.waila.redstone_clock.delay")
            .arguments(RebarArgument.of("delay", timing)));
        return display;
    }
}
