package io.github.tagdl.DoraMachines.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public class HydraulicWoodcutter extends RebarBlock implements
    DirectionalRebarBlock,
    TickingRebarBlock,
    GuiRebarBlock,
    LogisticRebarBlock,
    ProcessorRebarBlock,
    FluidBufferRebarBlock,
    VirtualInventoryRebarBlock
{   
    public static class Item extends RebarItem {

        public final double fluidPerCraft = getSettings().getOrThrow("fluid-per-craft", ConfigAdapter.INTEGER);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }
        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("fluid-per-craft", UnitFormat.MILLIBUCKETS.format(fluidPerCraft)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    private static final NamespacedKey BREAKING_LOG = new NamespacedKey(DoraMachines.getInstance(), "breaking_log");
    private static final ConfigSection settings = ConfigSection.fromSettings(DoraMachinesKeys.HYDRAULIC_WOODCUTTER);
    private int spend_time = (int) (settings.getOrThrow("break-per-log-consume-seconds", ConfigAdapter.FLOAT) * 20);
    private int max_break = settings.getOrThrow("max-break-amount", ConfigAdapter.INTEGER); 
    public final double buffer = settings.getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final double fluidPerCraft = settings.getOrThrow("fluid-per-craft", ConfigAdapter.INTEGER);
    private VirtualInventory outputInventory = new VirtualInventory(1);
    private List<BlockPosition> breaking_block = new ArrayList<>();
    public ItemStackBuilder faceStack = ItemStackBuilder.of(Material.IRON_BLOCK)
            .addCustomModelDataString(getKey() + ":face");
    @SuppressWarnings("unused")
    public HydraulicWoodcutter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(20);
        createFluidPoint(FluidPointType.INPUT, BlockFace.EAST, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.WEST, context, false);
        this.setFacing(context.getFacing().getOppositeFace());
        addEntity("drill", new ItemDisplayBuilder()
                .itemStack(faceStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0.5)
                        .scale(0.6, 0.6, 0.2)
                        .rotate(0, 0, Math.PI / 4))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }
    @SuppressWarnings("unused")
    public HydraulicWoodcutter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        breaking_block = new ArrayList<>(pdc.get(BREAKING_LOG, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BLOCK_POSITION)));
    }
    @Override
    public void postInitialise() {
        outputInventory.addPreUpdateHandler(event -> {
            if (event.isAdd() || event.isSwap()) event.setCancelled(!(event.getUpdateReason() instanceof MachineUpdateReason));
        });
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
    }
    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(BREAKING_LOG, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BLOCK_POSITION), breaking_block);
    }
    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # o m o # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('m', outputInventory)
                .addIngredient('o', GuiItems.output())
                .build();
    }
    @Override
    public void tick() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        if (isProcessing()) {
            if (fluidAmount(PylonFluids.HYDRAULIC_FLUID) < fluidPerCraft
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < fluidPerCraft) return;
            removeFluid(PylonFluids.HYDRAULIC_FLUID, fluidPerCraft);
            addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, fluidPerCraft);
            progressProcess(getTickInterval());
            return;
        }
        Block block = getBlock().getRelative(getFacing());
        if (block == null || block.isEmpty()) return;
        if (!Tag.LOGS.isTagged(block.getType()) || BlockStorage.isRebarBlock(block)
                || !block.getWorld().getWorldBorder().isInside(block.getLocation())) return;
        if (!breaking_block.isEmpty()) return;
        if (fluidAmount(PylonFluids.HYDRAULIC_FLUID) < fluidPerCraft
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < fluidPerCraft) return;
        logRecord(block);
        if (breaking_block.isEmpty()) return;
        startProcess(spend_time * breaking_block.size());
    }
    private void logRecord(Block block) {
        if (block == null || block.isEmpty() || !block.getChunk().isLoaded()) return;
        if (!Tag.LOGS.isTagged(block.getType()) || BlockStorage.isRebarBlock(block)
                || !block.getWorld().getWorldBorder().isInside(block.getLocation())
                || !new BlockBreakBlockEvent(block, getBlock(), new ArrayList<>()).callEvent()) return;
        if (breaking_block.size() > max_break || breaking_block.contains(new BlockPosition(block))) return;
        breaking_block.add(new BlockPosition(block));
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            logRecord(block.getRelative(face));
        }
    }
    @Override
    public void onProcessFinished() {
        if (breaking_block.isEmpty()) return;
        for (BlockPosition blockPosition : new ArrayList<>(breaking_block)) {
            Block block = blockPosition.getBlock();
            if (block == null || block.isEmpty() || !Tag.LOGS.isTagged(block.getType())) {
                continue;
            }
            List<ItemStack> drops = block.getDrops().stream().toList();
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
            if (!outputInventory.canHold(drops)) block.breakNaturally();
            else {
                block.setType(Material.AIR);
                drops.forEach(drop -> outputInventory.addItem(new MachineUpdateReason(), drop));
            }
        }
        breaking_block.clear();
        Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
            ItemStack holdingItemStack = outputInventory.getItem(0);
            if (holdingItemStack == null || holdingItemStack.isEmpty()) return;
            ItemStack removedItemStack = holdingItemStack.subtract();
            Matcher matcher = Pattern.compile("^(.+_)(LOG)$").matcher(removedItemStack.getType().name());
            if (matcher.matches()) getBlock().getRelative(getFacing())
                    .setType(Material.matchMaterial(matcher.group(1) + "SAPLING"));
        });
    }
    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("output", outputInventory);
    }
    @Override
    public void onBlockBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        VirtualInventoryRebarBlock.super.onBlockBreak(drops, context);
        FluidBufferRebarBlock.super.onBlockBreak(drops, context);
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (isProcessing()) display.add(ProgressBar.timeRemaining(getProcessTimeSeconds(), getProcessSecondsRemaining()));
        return display
            .add(ProgressBar.fluidContents(PylonFluids.HYDRAULIC_FLUID, buffer, fluidAmount(PylonFluids.HYDRAULIC_FLUID)))
            .add(ProgressBar.fluidContents(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID)));
    }
}
