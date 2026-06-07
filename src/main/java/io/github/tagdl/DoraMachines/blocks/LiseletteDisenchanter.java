package io.github.tagdl.DoraMachines.blocks;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.PreRebarBlockPlaceEvent;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent;
import xyz.xenondevs.invui.item.ItemBuilder;

public class LiseletteDisenchanter extends RebarBlock implements 
        DirectionalRebarBlock,
        ProcessorRebarBlock,
        FluidBufferRebarBlock,
        VirtualInventoryRebarBlock,
        LogisticRebarBlock,
        GuiRebarBlock,
        TickingRebarBlock
{
    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory bookInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(2);
    private final ItemStackBuilder progressItemStackBuilder = ItemStackBuilder.of(Material.CLOCK)
            .name(Component.translatable("doramachines.gui.liselette_disenchanter.progress"));
    private final ProgressItem progressItem = new ProgressItem(progressItemStackBuilder, false);
    private final Component EQUITMENT_DISPLAY = Component.translatable("doramachines.gui.liselette_disenchanter.equitment_name");
    private final Component BOOK_DISPLAY = Component.translatable("doramachines.gui.liselette_disenchanter.book_name");
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final double fluidPerCraft = getSettings().getOrThrow("fluid-per-craft", ConfigAdapter.INTEGER);
    private int timing = getSettings().getOrThrow("spend_seconds", ConfigAdapter.INTEGER) * 20;
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
    @SuppressWarnings("unused")
    public LiseletteDisenchanter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        addEntity("background2", new ItemDisplayBuilder()
                .itemStack(ItemStack.of(Material.ENCHANTING_TABLE))
                .transformation(new TransformBuilder()
                    .rotate(0, 0, 0)
                    .scale(1.01,1,1.01)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("background", new ItemDisplayBuilder()
                .itemStack(ItemStack.of(Material.PURPLE_CARPET))
                .transformation(new TransformBuilder()
                    .scale(Math.sqrt(2.0) / 2)
                    .translate(0, 0.8, 0)
                    .rotate(0, Math.PI / 4, 0)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("equitment", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                    .lookAlong(context.getFacing().getOppositeFace())
                    .scale(0.2)
                    .translate(new Vector3d(1.0, 1.35, 0))
                    .rotate(Math.PI / 2, 0, 0)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("enchant", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                    .lookAlong(context.getFacing().getOppositeFace())
                    .scale(0.2)
                    .translate(new Vector3d(-1.0, 1.35, 0))
                    .rotate(Math.PI / 2, 0, 0)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        setTickInterval(20);
        this.setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidBuffer(PylonFluids.OBSCYRA, buffer, true, false);
    }
    @SuppressWarnings("unused")
    public LiseletteDisenchanter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
    @Override
    public void postInitialise() {
        setProcessProgressItem(progressItem);
        inputInventory.addPreUpdateHandler(event -> onInputUpdate(event));
        bookInventory.addPreUpdateHandler(event -> onBookUpdate(event));
        outputInventory.addPreUpdateHandler(event -> onOutputUpdate(event));
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);

    }
    public void onInputUpdate(ItemPreUpdateEvent event){
        if (event.isAdd()) {
            boolean tempbool = event.getNewItem().getType() == Material.ENCHANTED_BOOK 
                || event.getNewItem().getType() == Material.BOOK;
            if (!tempbool) {
                getHeldEntity(ItemDisplay.class, "equitment").setItemStack(event.getNewItem().asOne());
            } else {
                event.setCancelled(true);
            }
        }
        if (event.isRemove()) getHeldEntity(ItemDisplay.class, "equitment").setItemStack(null);
    }
    public void onBookUpdate(ItemPreUpdateEvent event){
        if (event.isAdd()) {
            boolean tempbool = event.getNewItem().getType() != Material.ENCHANTED_BOOK 
                && event.getNewItem().getType() != Material.BOOK;
            if (!tempbool) {
                getHeldEntity(ItemDisplay.class, "enchant").setItemStack(event.getNewItem().asOne());
            } else {
                event.setCancelled(true);
            }
        }
        if (event.isRemove()) getHeldEntity(ItemDisplay.class, "enchant").setItemStack(null);
    }
    public void onOutputUpdate(ItemPreUpdateEvent event){
        if (event.isAdd()) event.setCancelled(!(event.getUpdateReason() instanceof MachineUpdateReason));
    }
    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "A A A A # B B B B",
                        "A I E A P B O O B",
                        "A A A A # B B B B"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('A', GuiItems.input())
                .addIngredient('B', GuiItems.output())
                .addIngredient('I', inputInventory, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName(EQUITMENT_DISPLAY))
                .addIngredient('E', bookInventory, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName(BOOK_DISPLAY))
                .addIngredient('P', progressItem)
                .addIngredient('O', outputInventory)
                .build();
    }
    @Override
    public void tick() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        if (isProcessing()) {
            if (bookInventory.isEmpty() || inputInventory.isEmpty()) stopProcess();
            if (fluidAmount(PylonFluids.OBSCYRA) < fluidPerCraft) return;
            progressProcess(getTickInterval());
            progressItem.notifyWindows();
            removeFluid(PylonFluids.OBSCYRA, fluidPerCraft);
            return;            
        }
        if (bookInventory.isEmpty() || inputInventory.isEmpty()) return;
        if (!outputInventory.canHold(inputInventory.getItem(0))) return;

        startProcess(timing);
    }
    @Override
    public void onProcessFinished() {
        progressItem.notifyWindows();
        ItemStack tempItemStack = inputInventory.getItem(0).clone();
        ItemStack tempBookItemStack = bookInventory.getItem(0).getType() == Material.ENCHANTED_BOOK
            ? bookInventory.getItem(0).clone() : ItemStack.of(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta esmc = (EnchantmentStorageMeta) tempBookItemStack.getItemMeta();
        inputInventory.getItem(0).getItemMeta().getEnchants().forEach((enchantment, level) -> {
            tempItemStack.removeEnchantment(enchantment);
            esmc.addStoredEnchant(enchantment, level, true);
        });
        tempBookItemStack.setItemMeta(esmc);
        outputInventory.addItem(new MachineUpdateReason(), tempItemStack);
        outputInventory.addItem(new MachineUpdateReason(), tempBookItemStack);
        Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
            inputInventory.setItemAmount(new MachineUpdateReason(), 0, 0);
            bookInventory.setItem(new MachineUpdateReason(), 0, 
                bookInventory.getItem(0).asQuantity(bookInventory.getItemAmount(0) - 1));
        });
    }
    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("equiment", inputInventory, "book", bookInventory, "output", outputInventory);
    }
    @Override
    public void onBlockBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        VirtualInventoryRebarBlock.super.onBlockBreak(drops, context);
        FluidBufferRebarBlock.super.onBlockBreak(drops, context);
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(isProcessing()
            ? Component.translatable("doramachines.item.liselette_disenchanter.waila.running")
                .arguments(
                    RebarArgument.of("time", 
                        Math.round(progressItem.getTotalTime().toSeconds() * (1.0 - progressItem.getProgress()))),
                    RebarArgument.of("input-bar", ProgressBar.fluidContents(
                        PylonFluids.OBSCYRA, 
                        fluidCapacity(PylonFluids.OBSCYRA), 
                        fluidAmount(PylonFluids.OBSCYRA)
                    ))
                )
            : Component.translatable("doramachines.item.liselette_disenchanter.waila.not_running")
                .arguments(RebarArgument.of("input-bar", ProgressBar.fluidContents(
                                PylonFluids.OBSCYRA, 
                                fluidCapacity(PylonFluids.OBSCYRA), 
                                fluidAmount(PylonFluids.OBSCYRA)
                            ))
                )
        );
    }
    public static final class PlaceListener implements Listener {
        @EventHandler
        private void onPlace(PreRebarBlockPlaceEvent e) {
            if (!e.getBlockSchema().getKey().equals(DoraMachinesKeys.LISELETTE_DISENCHANTER)) return;
            Slab slab = (Slab) e.getBlock().getBlockData();
            switch (slab.getType()) {
                case TOP -> {
                    slab.setType(Slab.Type.BOTTOM);
                    e.getBlock().setBlockData(slab);
                }
                case BOTTOM -> { /* Allow */ }
                case DOUBLE -> e.setCancelled(true);
            }
        }
    }
}
