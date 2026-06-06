package io.github.tagdl.DoraMachines.blocks;

import static io.github.pylonmc.pylon.util.PylonUtils.colorToTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import io.github.tagdl.DoraMachines.blocks.gui.MultiFluidSelector;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

public class PortableBackPack extends RebarBlock implements 
    DirectionalRebarBlock,
    FluidBufferRebarBlock,
    GuiRebarBlock
{
    public static class Item extends RebarItem implements
        InteractRebarItemHandler
    {
        private final ConfigSection config = ConfigSection.fromSettings(DoraMachinesKeys.PORTABLE_BACKPACK);
        private final double capacity = config.getOrThrow("capacity", ConfigAdapter.DOUBLE);
        private static final NamespacedKey INVENTORY_KEY = new NamespacedKey(DoraMachines.getInstance(), "backpack_inventory");
        private static final NamespacedKey NAME_KEY = new NamespacedKey(DoraMachines.getInstance(), "backpack_name");
        private static final NamespacedKey STORED_TYPE_KEY = new NamespacedKey(DoraMachines.getInstance(), "backpack_stored_type");
        private static final NamespacedKey STORED_AMOUNT_KEY = new NamespacedKey(DoraMachines.getInstance(), "backpack_stored_amount");
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }
        @Override
        public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority){
            if (event.getHand() != EquipmentSlot.HAND 
                    || event.useItemInHand() == Event.Result.DENY
                    || event.getAction().isLeftClick()
                    || event.getClickedBlock() != null) return;
            
            PagedGui<Inventory> gui = make();
            open(gui, event.getPlayer());
        }
        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                RebarArgument.of("type1", getStoredType().get(0).getName()),
                RebarArgument.of("amount1", UnitFormat.MILLIBUCKETS.format(getStoredAmount().get(0))),
                RebarArgument.of("type2", getStoredType().get(1).getName()),
                RebarArgument.of("amount2", UnitFormat.MILLIBUCKETS.format(getStoredAmount().get(1))),
                RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(capacity))
            );
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
                    RebarSerializers.COMPONENT, name));
        }
        public void setName(Component name) {
            getStack().editMeta(meta -> meta.itemName(name));
        }
        public Component getName() {
            return getStack().getPersistentDataContainer()
                .getOrDefault(NAME_KEY, RebarSerializers.COMPONENT, Component.translatable("doramachines.item.portable_backpack.name"));
        }
        public void setAll(List<RebarFluid> fluids, List<Double> d, VirtualInventory inventory, Component name) {
            getStack().editPersistentDataContainer(pdc -> {
                pdc.set(STORED_TYPE_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID), fluids);
                pdc.set(STORED_AMOUNT_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE), d);
                pdc.set(NAME_KEY, RebarSerializers.COMPONENT, name);
                pdc.set(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, inventory);
            });
        }
        public List<RebarFluid> getStoredType() {
            return getStack().getPersistentDataContainer()
                .getOrDefault(STORED_TYPE_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID),
                    new ArrayList<>(Arrays.asList(PylonFluids.WATER, PylonFluids.LAVA)));
        }
        public List<Double> getStoredAmount() {
            return getStack().getPersistentDataContainer()
                .getOrDefault(STORED_AMOUNT_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE),
                    new ArrayList<>(Arrays.asList(0.0, 0.0)));
        }
        public VirtualInventory getInventory() {
            return getStack().getPersistentDataContainer()
                .getOrDefault(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, new VirtualInventory(7 * 18));
        }
        public PagedGui<Inventory> make() {
            VirtualInventory inv = getInventory();
            BoundItem back = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                    .setName(Component.translatable("doramachines.gui.portable_backpack.backpage")))
                .addClickHandler((item, inner_gui, click) -> inner_gui.setPage(inner_gui.getPage() - 1))
                .build();
            BoundItem forward = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                    .setName(Component.translatable("doramachines.gui.portable_backpack.forwardpage")))
                .addClickHandler((item, inner_gui, click) -> inner_gui.setPage(inner_gui.getPage() + 1))
                .build();
            BoundItem craft = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.CRAFTING_TABLE)
                    .setName(Component.translatable("doramachines.gui.portable_backpack.craft")))
                .addClickHandler((item, inner_gui, click) -> 
                    inner_gui.getCurrentViewers().forEach(player -> player.openInventory(MenuType.CRAFTING.create(player))))
                .build();
            BoundItem casing = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(PylonItems.FLUID_TANK_CASING_STEEL.asOne())
                    .setName(Component.translatable("doramachines.gui.portable_backpack.casing"))
                    .clearLore())
                .addClickHandler((item, inner_gui, click) -> 
                    inner_gui.getCurrentViewers().forEach(player -> {
                        Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                            Window.builder()
                                .setUpperGui(casingGui())  
                                .setTitle(Component.translatable("doramachines.gui.portable_backpack.casing"))
                                .open(player);
                        });
                    }))
                .build();
            PagedGui<Inventory> gui = PagedGui.inventoriesBuilder()
                    .setStructure(
                            "U X X X X X X X A",
                            "C X X X X X X X A",
                            "A X X X X X X X A",
                            "A X X X X X X X A",
                            "T X X X X X X X A",
                            "D X X X X X X X A"
                    )
                    .addIngredient('A', GuiItems.background())
                    .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('C', craft)
                    .addIngredient('T', casing)
                    .setContent(List.of(inv))
                    .addIngredient('U', back)
                    .addIngredient('D', forward)
                    .build();
            inv.addPreUpdateHandler(event -> 
                event.setCancelled(RebarItem.fromStack(event.getNewItem()) instanceof Item));
            inv.addPostUpdateHandler(event -> {
            getStack().editPersistentDataContainer(pdc ->
                    pdc.set(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, inv));
            });
            return gui;
        }
        public Gui casingGui() {
            Gui gui = Gui.builder()
                .setStructure("# T C # # # t c #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('T', getStoredType().get(0).getItem())
                .addIngredient('t', getStoredType().get(1).getItem())
                .addIngredient('C', new innerStack(0))
                .addIngredient('c', new innerStack(1))
                .build();
            return gui;
        }
        private class innerStack extends AbstractItem {
        private int index;
        public innerStack(int index){
            this.index = index;
        }
        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            Material material;
            List<Component> lore = new ArrayList<>();
            if (getStoredAmount().get(index) == 0.0) {
                material = Material.BUCKET;  
                lore.add(Component.translatable("doramachines.gui.portable_backpack.capacity.empty"));
            } else {
                material = getStoredType().get(this.index).getTag(FluidTemperature.class) == FluidTemperature.HOT
                        ? Material.LAVA_BUCKET : Material.WATER_BUCKET;
                lore.add(Component.translatable("doramachines.gui.portable_backpack.capacity.not_empty")
                            .arguments(RebarArgument.of("storing", UnitFormat.MILLIBUCKETS.format(getStoredAmount().get(index))),
                                RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(capacity))
                        ));
            }
            return ItemStackBuilder.of(material)
                .name(Component.translatable("doramachines.gui.portable_backpack.capacity.name"))
                .lore(lore);
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {}
    }
        @Override
        public @NotNull RebarBlock place(@NotNull BlockCreateContext context) {
            PortableBackPack tank = (PortableBackPack) getSchema().place(context);
            tank.setAll(getStoredType(), getStoredAmount(), getInventory(), getName());
            for (int i = 0; i < getStoredType().size(); i++) {
                tank.createFluidBuffer(getStoredType().get(i), capacity, true, true);
                tank.setFluidCapacity(getStoredType().get(i), capacity);
                tank.setFluid(getStoredType().get(i), getStoredAmount().get(i));
            }
            return tank;
        }
    }
    private static final NamespacedKey STATUS_KEY = new NamespacedKey(DoraMachines.getInstance(), "block_status");
    private static final NamespacedKey FLUIDTYPE_KEY = new NamespacedKey(DoraMachines.getInstance(), "block_type");
    private static final NamespacedKey CAPACITY_KEY = new NamespacedKey(DoraMachines.getInstance(), "block_capacity");
    private static final NamespacedKey COMPONENT_KEY = new NamespacedKey(DoraMachines.getInstance(), "block_component");
    private static final NamespacedKey INVENTORY_KEY = new NamespacedKey(DoraMachines.getInstance(), "block_inventory");
    private List<RebarFluid> stored_type = new ArrayList<>(Arrays.asList(PylonFluids.WATER, PylonFluids.LAVA));
    private List<Double> stored_amount = new ArrayList<>(Arrays.asList(0.0, 0.0));
    private Component component;
    private VirtualInventory inventory;
    private List<Boolean> running = new ArrayList<>(Arrays.asList(false, false));
    private final ConfigSection config = ConfigSection.fromSettings(DoraMachinesKeys.PORTABLE_BACKPACK);
    private final double capacity = config.getOrThrow("capacity", ConfigAdapter.DOUBLE);
    private ItemStack offItemStack = new ItemStackBuilder(ItemStack.of(Material.RED_STAINED_GLASS_PANE))
                .name(Component.text("OFF").color(colorToTextColor(Color.RED)))
                .lore(Component.translatable("doramachines.gui.portable_backpack.lore"))
                .build();
    private ItemStack onItemStack = new ItemStackBuilder(ItemStack.of(Material.LIME_STAINED_GLASS_PANE))
                .name(Component.text("ON").color(colorToTextColor(Color.LIME)))
                .lore(Component.translatable("doramachines.gui.portable_backpack.lore"))
                .build();
    private VirtualInventory statusInventory = new VirtualInventory(new ItemStack[]{offItemStack, offItemStack});
    @SuppressWarnings("unused")
    public PortableBackPack(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        this.setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
    }
    @SuppressWarnings("unused")
    public PortableBackPack(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        stored_type = new ArrayList<>(pdc.get(FLUIDTYPE_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID)));
        stored_amount = new ArrayList<>(pdc.get(CAPACITY_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE)));
        running = new ArrayList<>(pdc.get(STATUS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BOOLEAN)));
        inventory = pdc.get(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY);
        component = pdc.get(COMPONENT_KEY, RebarSerializers.COMPONENT);
    }
    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(FLUIDTYPE_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID), stored_type);
        pdc.set(CAPACITY_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE), stored_amount);
        pdc.set(STATUS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BOOLEAN), running);
        pdc.set(INVENTORY_KEY, RebarSerializers.VIRTUAL_INVENTORY, inventory);
        pdc.set(COMPONENT_KEY, RebarSerializers.COMPONENT, component);
    }
    @Override
    public void postInitialise() {
        statusInventory.addPreUpdateHandler(event -> event.setCancelled(!(event.getUpdateReason() instanceof MachineUpdateReason)));
        statusInventory.addClickHandler(event -> {
            event.getInventory().setItem(new MachineUpdateReason(), event.getSlot(),
                running.get(event.getSlot()) ? offItemStack : onItemStack);
            running.set(event.getSlot(), !running.get(event.getSlot()));
        });
    }
    public void setAll(List<RebarFluid> fluids, List<Double> d, VirtualInventory inventory, Component component) {
        this.stored_type = new ArrayList<>(fluids);
        this.stored_amount = new ArrayList<>(d);
        this.inventory = inventory;
        this.component = component;
    }
    public Gui getGui(){
        Gui gui = Gui.builder()
                .setStructure("# T . O # t . O #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('T', new typeStack(0))
                .addIngredient('t', new typeStack(1))
                .addIngredient('O', statusInventory)
                .build();
        gui.setItem(2, new capacityStack(0));
        gui.setItem(6, new capacityStack(1));
        return gui;
    }
    private class typeStack extends AbstractItem {
        private int index;
        public typeStack(int index){
            this.index = index;
        }
        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            return ItemStackBuilder.of(stored_type.get(index).getItem());
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            PagedGui<Inventory> fluidGui = MultiFluidSelector.make(this::setFluidInner);
            Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                Window.builder()
                    .setUpperGui(fluidGui)  
                    .setTitle(Component.translatable("doramachines.gui.portable_backpack.select"))  
                    .open(player);
                notifyWindows();
            });
        }
        public void setFluidInner(RebarFluid fluid, Player player){
            if (fluid.equals(stored_type.get(index == 0 ? 1 : 0))) {
                player.sendMessage(Component.translatable("doramachines.message.portable_backpack.samefluid"));
            } else {
                deleteFluidBuffer(stored_type.get(index));
                stored_type.set(index, fluid);
                stored_amount.set(index, 0.0);
                createFluidBuffer(fluid, capacity, true, true);
                setFluidCapacity(fluid, capacity);
                setFluid(fluid, 0.0);
            }
            Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                Window.builder()
                    .setUpperGui(getGui())  
                    .setTitle(Component.translatable("doramachines.item.portable_backpack.name"))  
                    .open(player);
                notifyWindows();
            });
        }
    }
    private class capacityStack extends AbstractItem {
        private int index;
        public capacityStack(int index){
            this.index = index;
        }
        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            Material material;
            List<Component> lore = new ArrayList<>();
            if (stored_amount.get(index) == 0.0) {
                material = Material.BUCKET;  
                lore.add(Component.translatable("doramachines.gui.portable_backpack.capacity.empty"));
            } else {
                material = stored_type.get(this.index).getTag(FluidTemperature.class) == FluidTemperature.HOT
                        ? Material.LAVA_BUCKET : Material.WATER_BUCKET;
                lore.add(Component.translatable("doramachines.gui.portable_backpack.capacity.not_empty")
                            .arguments(RebarArgument.of("storing", UnitFormat.MILLIBUCKETS.format(stored_amount.get(index))),
                                RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(capacity))
                            ));
            }
            return ItemStackBuilder.of(material)
                .name(Component.translatable("doramachines.gui.portable_backpack.capacity.name"))
                .lore(lore);
        }
        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {}
    }
    @Override
    public @NotNull Gui createGui() {
        return getGui();
    }
    @Override
    public @NotNull List<Pair<RebarFluid, Double>> getSuppliedFluids() {
        List<Pair<RebarFluid, Double>> fluidList = new ArrayList<>();
        fluidList.add(new Pair<RebarFluid,Double>(stored_type.get(0),
            running.get(0) ? stored_amount.get(0): 0.0));
        fluidList.add(new Pair<RebarFluid,Double>(stored_type.get(1),
            running.get(1) ? stored_amount.get(1): 0.0));
        return fluidList;
    }
    @Override  
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidAdded(fluid, amount);
        if (stored_type.get(0).equals(fluid)) stored_amount.set(0, stored_amount.get(0) + amount);
        else stored_amount.set(1, stored_amount.get(1) + amount);
        if (statusInventory.getWindows() != null || !statusInventory.getWindows().isEmpty()) {
            for (Window window : statusInventory.getWindows()) {
                    int index = stored_type.get(0).equals(fluid) ? 0 : 1;
                    int slot = stored_type.get(0).equals(fluid) ? 2 : 6;
                    capacityStack item = new capacityStack(index);
                    window.getGuis().get(0).setItem(slot, item);
                    item.notifyWindows();
            }
        }
    }
    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidRemoved(fluid, amount);
        if (stored_type.get(0).equals(fluid)) stored_amount.set(0, stored_amount.get(0) - amount);
        else stored_amount.set(1, stored_amount.get(1) - amount);
        if (statusInventory.getWindows() != null || !statusInventory.getWindows().isEmpty()) {
            for (Window window : statusInventory.getWindows()) {
                    int index = stored_type.get(0).equals(fluid) ? 0 : 1;
                    int slot = stored_type.get(0).equals(fluid) ? 2 : 6;
                    capacityStack item = new capacityStack(index);
                    window.getGuis().get(0).setItem(slot, item);
                    item.notifyWindows();
            }
        }
    }
    @Override
    public @Nullable ItemStack getDropItem(@NotNull BlockBreakContext context) {
        return getDrop();
    }
    @Override
    public @Nullable ItemStack getPickItem(@NotNull Player player) {
        return getDrop();
    }
    public ItemStack getDrop() {
        ItemStack stack = RebarRegistry.ITEMS.getOrThrow(getKey()).getItemStack();
        Item item = new Item(stack);
        item.setAll(stored_type, stored_amount, inventory, component);
        item.setName(component);
        return stack;
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(Component.translatable("doramachines.item.portable_backpack.waila")
                .arguments(
                    RebarArgument.of("type1", stored_type.get(0).getName()),
                    RebarArgument.of("type2", stored_type.get(1).getName()),
                    RebarArgument.of("input-bar1", PylonUtils.createFluidAmountBar(
                            fluidAmount(stored_type.get(0)),
                            capacity,
                            20,
                            stored_type.get(0).getColor()
                    )),
                    RebarArgument.of("input-bar2", PylonUtils.createFluidAmountBar(
                            fluidAmount(stored_type.get(1)),
                            capacity,
                            20,
                            stored_type.get(1).getColor()
                    ))
                ));
    }
}
