package io.github.tagdl.DoraMachines.blocks;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import kotlin.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankCasing;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.blocks.gui.MultiFluidSelector;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

public class MultiFluidTank extends RebarBlock implements 
        GuiRebarBlock,
        FluidBufferRebarBlock,
        VirtualInventoryRebarBlock,
        LogisticRebarBlock,
        DirectionalRebarBlock
{
    private static final NamespacedKey STATUS_KEY = pylonKey("status_running");
    private static final NamespacedKey FLUIDTYPE_KEY = pylonKey("fluid_type");
    private static final NamespacedKey CAPACITY_KEY = pylonKey("fluid_capacity");

    @Getter
    private int[] running = {-1, -1, -1, -1};
    @Getter
    private List<RebarFluid> fluidtype = Arrays.asList(
        PylonFluids.WATER,PylonFluids.WATER,PylonFluids.WATER,PylonFluids.WATER);
    @Getter
    private List<Double> fluidcapacity = Arrays.asList(0.0, 0.0, 0.0, 0.0);
    
    public final ItemStackBuilder casingStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":fluid_tank_casing")
        .name(Component.translatable("doramachines.gui.multi_fluid_tank.fluid_tank_casing"));
    public final ItemStackBuilder typeStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":fluid_type")
        .name(Component.translatable("doramachines.gui.multi_fluid_tank.fluid_type"));
    private final VirtualInventory casingInventory = new VirtualInventory(4);
    
    private final int maxHeight = getSettings().getOrThrow("max-height", ConfigAdapter.INTEGER);
    
    @SuppressWarnings("unused")
    public MultiFluidTank(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        this.setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
    }
    @SuppressWarnings("unused")
    public MultiFluidTank(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        fluidtype = new ArrayList<>(pdc.get(FLUIDTYPE_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID)));
        fluidcapacity = new ArrayList<>(pdc.get(CAPACITY_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE)));
        running = pdc.get(STATUS_KEY, RebarSerializers.INTEGER_ARRAY);
        for (RebarFluid itemFluid : RebarRegistry.FLUIDS.getValues()) {
            double tempAmount = 0.0;
            for(int i = 0; i < 4; i++) {
                if (itemFluid.equals(fluidtype.get(i))) {
                    tempAmount += fluidcapacity.get(i);
                }
            }
            if (fluidtype.contains(itemFluid)) {
                deleteFluidBuffer(itemFluid);
                createFluidBuffer(itemFluid, tempAmount, true, true);
                setFluid(itemFluid, tempAmount);
            }
        }
    }
    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(FLUIDTYPE_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID), fluidtype);
        pdc.set(CAPACITY_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE), fluidcapacity);
        pdc.set(STATUS_KEY, RebarSerializers.INTEGER_ARRAY, running);
    }
    public void postInitialise() {
        for (RebarFluid itemFluid : RebarRegistry.FLUIDS.getValues()) {
            double tempAmount = 0.0;
            for(int i = 0; i < 4; i++) {
                if (itemFluid.equals(fluidtype.get(i))) {
                    tempAmount += fluidcapacity.get(i);
                }
            }
            if (fluidtype.contains(itemFluid)) {
                deleteFluidBuffer(itemFluid);
                createFluidBuffer(itemFluid, tempAmount, true, true);
                setFluid(itemFluid, tempAmount);
            }
        }
        casingInventory.addPostUpdateHandler(event -> onUpdate(event));
        createLogisticGroup("filter", LogisticGroupType.INPUT, casingInventory);
    }
    private void onUpdate(ItemPostUpdateEvent event){
        CapacityItem item = new CapacityItem(event.getSlot(), event.getNewItem() == null ? 0 : event.getNewItem().getAmount());
        if (RebarItem.fromStack(event.getNewItem()) 
                    instanceof FluidTankCasing.Item castingItem) {
            fluidcapacity.set(event.getSlot(), min(fluidcapacity.get(event.getSlot())
                    , castingItem.capacity * min(maxHeight 
                        , event.getNewItem() == null ? 0 : event.getNewItem().getAmount())));
        } else {
            fluidcapacity.set(event.getSlot(), 0.0);
        }
        int targetSlot = 10 + event.getSlot() * 2;
        if (targetSlot >= 0 && targetSlot < 36) {
            for (Window window : event.getInventory().getWindows()) {
                if (window.getGuis().size() > 0) {
                    window.getGuis().get(0).setItem(targetSlot, item);
                    item.notifyWindows();
                }
            }
        }
        updateCapacity();
    }
    public Gui getGui(){
        Gui gui = Gui.builder()
                .setStructure(
                        "I T I T I T I T I",
                        "# . # . # . # . #", //FluidCapacity
                        "H . H . H . H . H", //FluidType
                        "# . # . # . # . #"  //GateItem
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', casingStack)
                .addIngredient('T', casingInventory)
                .addIngredient('H', typeStack)
                .build();
        int[] capacitySlots = {10, 12, 14, 16};
        int[] typeSlots = {19, 21, 23, 25};
        int[] gateSlots = {28, 30, 32, 34};
        for (int i = 0; i < 4; i++) {
            gui.setItem(capacitySlots[i], new CapacityItem(i));
            gui.setItem(typeSlots[i], new FluidTypeItem(i));
            gui.setItem(gateSlots[i], new GateItem(i));
        }
        return gui;
    }
    @Override
    public @NotNull Gui createGui() {
        return getGui();
    }
    private class GateItem extends AbstractItem {
        private int index;
        public GateItem(int index){
            this.index = index;
        }
        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            Material material;
            List<Component> lore = new ArrayList<>();

            if (running[index] == 1) { //1 is turn on, -1 is turn off
                material = Material.GREEN_STAINED_GLASS_PANE;  
                lore.add(Component.translatable("doramachines.gui.multi_fluid_tank.status.running"));
            } else {
                material = Material.YELLOW_STAINED_GLASS_PANE;
                lore.add(Component.translatable("doramachines.gui.multi_fluid_tank.status.not_running"));
            }
            return ItemStackBuilder.of(material)
                .name(Component.translatable("doramachines.gui.multi_fluid_tank.status.name"))
                .lore(lore);
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            setRunning(this.index, -running[index]);
            createFluidB(fluidtype.get(this.index));
            Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                notifyWindows();
            });
        }
    }
    private class FluidTypeItem extends AbstractItem {
        private int index;
        public FluidTypeItem(int index){
            this.index = index;
        }
        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            return ItemStackBuilder.of((fluidtype.get(this.index) == null) ? PylonFluids.WATER.getItem() : fluidtype.get(this.index).getItem());
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            PagedGui<Inventory> fluidGui = MultiFluidSelector.make(this::setFluid);
            Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                new ChangePage(fluidGui, player);
                notifyWindows();
            });
        }
        public void setFluid(RebarFluid fluid, Player player){
            if (fluid != fluidtype.get(index)) {
                fluidtype.set(this.index, fluid);
                fluidcapacity.set(this.index, 0.0);
                createFluidB(fluid);
            }
            Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                new ChangePage(getGui(), player);
                notifyWindows();
            });
        }
    }
    private class CapacityItem extends AbstractItem {
        private int index;
        private double maxCapacity = 0;
        public CapacityItem(int index){
            this.maxCapacity = 0;
            this.index = index;
        }
        public CapacityItem(int index, int amount){
            this.maxCapacity = min(amount, maxHeight);
            this.index = index;
        }
        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            Material material;
            List<Component> lore = new ArrayList<>();

            if (RebarItem.fromStack(casingInventory.getItem(this.index)) 
                    instanceof FluidTankCasing.Item castingItem) {
                this.maxCapacity = min(casingInventory.getItemAmount(this.index), maxHeight) * castingItem.capacity;
            } else {
                this.maxCapacity = 0;
            }
            lore.add(Component.translatable("doramachines.gui.multi_fluid_tank.capacity.max_capacity")
                        .arguments(RebarArgument.of("max", this.maxCapacity)));
            
            if (min(fluidcapacity.get(this.index), this.maxCapacity) == 0.0) {
                material = Material.BUCKET;  
                lore.add(Component.translatable("doramachines.gui.multi_fluid_tank.capacity.empty"));
            } else {
                material = fluidtype.get(this.index).getTag(FluidTemperature.class) == FluidTemperature.HOT
                        ? Material.LAVA_BUCKET : Material.WATER_BUCKET;
                lore.add(Component.translatable("doramachines.gui.multi_fluid_tank.capacity.not_empty")
                            .arguments(RebarArgument.of("capacity", min(fluidcapacity.get(this.index), this.maxCapacity))));
            }
            return ItemStackBuilder.of(material)
                .name(Component.translatable("doramachines.gui.multi_fluid_tank.capacity.name"))
                .lore(lore);
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {}
    }
    public class ChangePage{
        public ChangePage(PagedGui<Inventory> gui, Player player){
            Window.builder()
                .setUpperGui(gui)  
                .setTitle(Component.translatable("doramachines.gui.multi_fluid_tank.name"))  
                .open(player);
        }
        public ChangePage(Gui gui, Player player){
            Window.builder()
                .setUpperGui(gui)  
                .setTitle(Component.translatable("doramachines.gui.multi_fluid_tank.name"))  
                .open(player);
        }
    }
    public void updateCapacity() {
        for (RebarFluid itemFluid : RebarRegistry.FLUIDS.getValues()) {
            createFluidB(itemFluid);
        }
    }
    public void createFluidB(RebarFluid fluid) { //tempamount有问题
        double tempAmount = 0.0;
        if (hasFluid(fluid) || fluidtype.contains(fluid)) {
            for (int i = 0; i < 4; i++) {
                
                if (fluidtype.get(i) != fluid) continue;
                tempAmount += fluidcapacity.get(i);
            }
            deleteFluidBuffer(fluid);
        }
        createFluidBuffer(fluid, 0, fluidtype.contains(fluid), true);
        for(int i = 0; i < 4; i++) {
            if (RebarItem.fromStack(casingInventory.getItem(i)) instanceof FluidTankCasing.Item castingItem) {
                if (fluid.equals(fluidtype.get(i))) {
                    setFluidCapacity(fluid, ((fluidtype.indexOf(fluid) != i) ? fluidCapacity(fluid) : 0.0)
                            + castingItem.capacity * min(casingInventory.getItemAmount(i), maxHeight));
                    fluidcapacity.set(i, min(fluidcapacity.get(i),
                            castingItem.capacity * min(casingInventory.getItemAmount(i), maxHeight)));
                }
            } else {
                if (fluid.equals(fluidtype.get(i))) {
                    setFluidCapacity(fluid, (fluidtype.indexOf(fluid) != i) ? fluidCapacity(fluid) : 0.0);
                    fluidcapacity.set(i, 0.0);
                }
            }
        }
        setFluid(fluid, min(tempAmount, fluidCapacity(fluid)));
    }
    @Override  
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidAdded(fluid, amount);
        double tempamount = amount;
        double oldcapa;
        if (casingInventory.isEmpty()) return;
        for(int i = 0; i < 4; i++) {
            if (fluid != fluidtype.get(i)) continue;
            if (RebarItem.fromStack(casingInventory.getItem(i)) 
                    instanceof FluidTankCasing.Item castingItem) {
                if (castingItem.allowedTemperatures.contains(fluid.getTag(FluidTemperature.class))) {
                    if (fluidcapacity.get(i) < castingItem.capacity * min(maxHeight, casingInventory.getItemAmount(i))
                            && tempamount > 0) {
                        oldcapa = fluidcapacity.get(i);
                        fluidcapacity.set(i, min(fluidcapacity.get(i) + tempamount, castingItem.capacity * maxHeight));
                        tempamount = oldcapa + tempamount - fluidcapacity.get(i);
                    }
                }
            }
            if (casingInventory.getWindows() != null || !casingInventory.getWindows().isEmpty()) {
                for (Window window : casingInventory.getWindows()) {
                    CapacityItem item = new CapacityItem(i);
                    window.getGuis().get(0).setItem(10 + i * 2, item);
                    item.notifyWindows();
                }
            }
        }
    }
    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidRemoved(fluid, amount);
        double tempamount = amount;
        double tempcapacity = 0.0;
        for(int i = 0; i < 4; i++){
            if (fluid != fluidtype.get(i)) continue;
            if (running[i] < 0) continue;
            if (tempamount <= 0) continue;
            tempcapacity = fluidcapacity.get(i);
            fluidcapacity.set(i, max(0.0, fluidcapacity.get(i) - tempamount));
            tempamount -= (tempcapacity - fluidcapacity.get(i));
            if (casingInventory.getWindows() != null || !casingInventory.getWindows().isEmpty()) {
                for (Window window : casingInventory.getWindows()) {
                    CapacityItem item = new CapacityItem(i);
                    window.getGuis().get(0).setItem(10 + i * 2, item);
                    item.notifyWindows();
                }
            }
        }
    }
    @Override
    public @NotNull List<Pair<RebarFluid, Double>> getSuppliedFluids() {
        List<Pair<RebarFluid, Double>> fluidList = new ArrayList<>();
        List<Integer> templist = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(templist);
        for (int i = 0; i < 4; i++) {
            fluidList.add(new Pair<RebarFluid, Double>(fluidtype.get(templist.get(i)),
                running[templist.get(i)] < 0 ? 0 : fluidcapacity.get(templist.get(i))));
        }
        return fluidList;
    }
    public void setRunning(int index, int status) {
        this.running[index] = status;
    }
    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("casing", casingInventory);
    }
    @Override
    public void onBlockBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        VirtualInventoryRebarBlock.super.onBlockBreak(drops, context);
        FluidBufferRebarBlock.super.onBlockBreak(drops, context);
    }
}
