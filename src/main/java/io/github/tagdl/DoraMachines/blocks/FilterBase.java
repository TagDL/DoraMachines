package io.github.tagdl.DoraMachines.blocks;

import static java.lang.Math.random;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarInventoryBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.WeightedSet;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import io.github.tagdl.DoraMachines.items.Filter;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.OperationCategory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent;

import org.jetbrains.annotations.Nullable;

public class FilterBase extends RebarBlock implements
        RebarDirectionalBlock,
        RebarInventoryBlock,
        RebarVirtualInventoryBlock,
        RebarProcessor,
        RebarLogisticBlock,
        RebarTickingBlock
{
    public final ItemStackBuilder filterStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":filter")
            .name(Component.translatable("doramachines.gui.filter"));
    private final VirtualInventory filterInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(16);
    private static Config settings = Settings.get(DoraMachinesKeys.FILTER_BASE);
    private static WeightedSet<ItemStack> canOutput = settings.getOrThrow("filter_things", ConfigAdapter.WEIGHTED_SET.from(ConfigAdapter.ITEM_STACK));
    private final int doTick = 20;
    private ItemStack filterThing = new ItemStack(Material.AIR);
    @SuppressWarnings("unused")
    public FilterBase(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(doTick);
        this.setFacing(context.getFacing());
    }
    @SuppressWarnings("unused")
    public FilterBase(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
    @Override
    public void postInitialise() {
        filterInventory.addPreUpdateHandler(event -> onFilterUpdate(event));
        filterInventory.setGuiPriority(OperationCategory.ADD, 2);
        createLogisticGroup("filter", LogisticGroupType.INPUT, filterInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
    }
    private void onFilterUpdate(ItemPreUpdateEvent event){
        if (event.isAdd()) event.setCancelled(!(RebarItem.fromStack(event.getNewItem()) instanceof Filter));
    }
    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "I I I O O O O O O",
                        "I F I O E E E E O",
                        "I I I O E E E E O",
                        "# # # O E E E E O",
                        "# # # O E E E E O",
                        "# # # O O O O O O"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('F', filterInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('I', filterStack)
                .addIngredient('E', outputInventory)
                .build();
    }
    @Override
    public void tick() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        if (filterInventory == null || outputInventory == null) return;
        ItemStack filterItemStack = filterInventory.getItem(0);
        if (filterItemStack == null) {
                    filterThing = new ItemStack(Material.AIR);
                    return;
        }
        if (!(RebarItem.fromStack(filterItemStack) instanceof Filter)
            || !inWater(getBlock())) {
                    filterThing = new ItemStack(Material.AIR);
                    return;
        }
        if (isProcessing()) {
            progressProcess(getTickInterval());
            return;            
        }
        Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
            startProcess(doTick);
            RebarUtils.damageItem(filterItemStack, 1, getBlock().getWorld());
            filterInventory.setItem(new MachineUpdateReason(), 0, filterItemStack);
        });
    }
    
    private ItemStack RandomAdd() {
        Double weightNow = 0.0;
        Double totalLength = 0.0;
        for (WeightedSet.Element<ItemStack> element : canOutput) {
            totalLength += element.weight();
        }
        Double randomDouble = random() * totalLength;
        for (WeightedSet.Element<ItemStack> element : canOutput) {
            weightNow += element.weight();
            if (weightNow >= randomDouble) {
                filterThing = new ItemStack(element.element().clone());
                return new ItemStack(element.element().clone());
            }
        };
        return new ItemStack(Material.AIR);
    }
    private boolean inWater(Block block) {
        if (block == null || !block.getChunk().isLoaded()) return false;
        BlockData filterData = block.getBlockData();
        if (!(filterData instanceof Waterlogged waterlogged)) return false;
        return waterlogged.isWaterlogged();
    }
    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        if (inWater(getBlock())) getBlock().setType(Material.WATER);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }
    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("filter", filterInventory, "output", outputInventory);
    }
    @Override
    public void onProcessFinished() {
        ItemStack item = RandomAdd();
        if (item.getType() == Material.AIR 
                || !outputInventory.canHold(item)) {
                    filterThing = new ItemStack(Material.AIR);
                    return;
                }
        outputInventory.addItem(new MachineUpdateReason(), item);
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(Component.translatable("doramachines.item.filter_base.waila")
                .arguments(
                    RebarArgument.of("contents", 
                        filterThing.equals(new ItemStack(Material.AIR))
                            ? Component.translatable("doramachines.waila.filter_base.none")
                            : Component.translatable("doramachines.waila.filter_base.filterwhat")
                                .arguments(RebarArgument.of("filter", filterThing.effectiveName()))
                    )
                )
        );
    }
}
