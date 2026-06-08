package io.github.tagdl.DoraMachines.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.Rebar;
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
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import lombok.Getter;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public class Woodcutter extends RebarBlock implements
    DirectionalRebarBlock,
    TickingRebarBlock,
    GuiRebarBlock,
    LogisticRebarBlock,
    ProcessorRebarBlock,
    FluidBufferRebarBlock,
    VirtualInventoryRebarBlock
{   
    private static final NamespacedKey BREAKING_LOG = new NamespacedKey(DoraMachines.getInstance(), "breaking_log");
    private static final ConfigSection settings = ConfigSection.fromSettings(DoraMachinesKeys.WOODCUTTER);
    private int spend_time = settings.getOrThrow("break-per-log-consume-seconds", ConfigAdapter.INTEGER) * 20;
    private int max_break = settings.getOrThrow("max-break-amount", ConfigAdapter.INTEGER); 
    public final double buffer = settings.getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final double fluidPerCraft = settings.getOrThrow("fluid-per-craft", ConfigAdapter.INTEGER);
    private VirtualInventory outputInventory = new VirtualInventory(1);
    private List<BlockPosition> breaking_block = new ArrayList<>();
    @SuppressWarnings("unused")
    public Woodcutter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(20);
        createFluidPoint(FluidPointType.INPUT, BlockFace.EAST, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.WEST, context, false);
        this.setFacing(context.getFacing().getOppositeFace());
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }
    @SuppressWarnings("unused")
    public Woodcutter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        breaking_block = new ArrayList<>(pdc.get(BREAKING_LOG, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BLOCK_POSITION)));
    }
    @Override
    public void postInitialise() {
        outputInventory.addPreUpdateHandler(event -> 
            event.setCancelled(event.isAdd() && !(event.getUpdateReason() instanceof MachineUpdateReason)));
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
    }
    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(BREAKING_LOG, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BLOCK_POSITION), breaking_block);
    }
    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # m # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('m', outputInventory)
                .build();
    }
    @Override
    public void tick() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        if (isProcessing()) {
            removeFluid(PylonFluids.HYDRAULIC_FLUID, fluidPerCraft);
            addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, fluidPerCraft);
            return;
        }
        Block block = getBlock().getRelative(getFacing());
        if (block == null || block.isEmpty()) return;
        if (!Tag.LOGS.isTagged(block.getType()) || BlockStorage.isRebarBlock(block)
                || !block.getWorld().getWorldBorder().isInside(block.getLocation())) return;
        if (!breaking_block.isEmpty()) return;
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
    public void onProcessFinished() { //TODO
        if (breaking_block.isEmpty()) return;
        for (BlockPosition blockPosition : breaking_block) {
            breaking_block.remove(blockPosition);
            Block block = blockPosition.getBlock();
            if (block == null || block.isEmpty() || !Tag.LOGS.isTagged(block.getType())) continue;
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
            block.setType(Material.AIR);
            List<ItemStack> drops = block.getDrops().stream().toList();
            if (!outputInventory.canHold(drops)) block.breakNaturally();
            else drops.forEach(drop -> outputInventory.addItem(new MachineUpdateReason(), drop));
        }
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
}
