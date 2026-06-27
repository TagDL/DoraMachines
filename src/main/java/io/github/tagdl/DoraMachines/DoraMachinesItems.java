package io.github.tagdl.DoraMachines;

import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.tagdl.DoraMachines.blocks.LiseletteDisenchanter;
import io.github.tagdl.DoraMachines.blocks.LiseletteEnchanter;
import io.github.tagdl.DoraMachines.blocks.PortableBackPack;
import io.github.tagdl.DoraMachines.blocks.RedstoneLink;
import io.github.tagdl.DoraMachines.blocks.DieselWoodcutter;
import io.github.tagdl.DoraMachines.blocks.HydraulicWoodcutter;
import io.github.tagdl.DoraMachines.guide.FilterPage;
import io.github.tagdl.DoraMachines.items.Dagger;
import io.github.tagdl.DoraMachines.items.ExplosionPickaxe;
import io.github.tagdl.DoraMachines.items.ExplosionShovel;
import io.github.tagdl.DoraMachines.items.Filter;
import io.github.tagdl.DoraMachines.items.GoldenBoneMeal;
import io.github.tagdl.DoraMachines.items.LinkedConnector;
import io.github.tagdl.DoraMachines.items.PlanC;
import io.github.tagdl.DoraMachines.items.RoadTool;
import io.github.tagdl.DoraMachines.items.RuckusMaker;
import io.github.tagdl.DoraMachines.items.Slingshot;
import io.github.tagdl.DoraMachines.items.UnbreakableRune;
import io.github.tagdl.DoraMachines.items.VoidPicker;
import io.papermc.paper.datacomponent.DataComponentTypes;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public final class DoraMachinesItems {

    public static final ItemStack EXPLOSION_PICKAXE = ItemStackBuilder.rebar(Material.NETHERITE_PICKAXE, DoraMachinesKeys.EXPLOSION_PICKAXE)
                .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(DoraMachinesKeys.EXPLOSION_PICKAXE).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    public static final ItemStack EXPLOSION_SHOVEL = ItemStackBuilder.rebar(Material.NETHERITE_SHOVEL, DoraMachinesKeys.EXPLOSION_SHOVEL)
                .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(DoraMachinesKeys.EXPLOSION_SHOVEL).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    public static final ItemStack UNBREAKABLE_RUNE = ItemStackBuilder.rebar(Material.FIREWORK_STAR, DoraMachinesKeys.UNBREAKABLE_RUNE)
                .set(DataComponentTypes.UNBREAKABLE)
            .build();
    public static final ItemStack GOLDEN_BONE_MEAL = ItemStackBuilder.rebar(Material.BONE_MEAL, DoraMachinesKeys.GOLDEN_BONE_MEAL)
            .build();
    public static final ItemStack INDUSTRIAL_MINER = ItemStackBuilder.rebar(Material.BLAST_FURNACE, DoraMachinesKeys.INDUSTRIAL_MINER)
            .build();
    public static final ItemStack FILTER_BASE = ItemStackBuilder.rebar(Material.PRISMARINE_WALL, DoraMachinesKeys.FILTER_BASE)
            .build();
    public static final ItemStack FILTER = ItemStackBuilder.rebar(Material.FISHING_ROD, DoraMachinesKeys.FILTER)
            .build();
    public static final ItemStack PLAN_C = ItemStackBuilder.rebar(Material.CRIMSON_BUTTON, DoraMachinesKeys.PLAN_C)
            .build();
    public static final ItemStack MULTI_FLUID_TANK = ItemStackBuilder.rebar(Material.LIGHT_BLUE_STAINED_GLASS, DoraMachinesKeys.MULTI_FLUID_TANK)
            .build();
    public static final ItemStack TIMING_CHARGER = ItemStackBuilder.rebar(Material.RED_GLAZED_TERRACOTTA, DoraMachinesKeys.TIMING_CHARGER)
            .build();
    public static final ItemStack LISELETTE_ENCHANTER = ItemStackBuilder.rebar(Material.POLISHED_DIORITE_SLAB, DoraMachinesKeys.LISELETTE_ENCHANTER)
            .build();
    public static final ItemStack LISELETTE_DISENCHANTER = ItemStackBuilder.rebar(Material.POLISHED_ANDESITE_SLAB, DoraMachinesKeys.LISELETTE_DISENCHANTER)
            .build();
    public static final ItemStack ROAD_TOOL = ItemStackBuilder.rebar(Material.NETHER_STAR, DoraMachinesKeys.ROAD_TOOL)
                .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    public static final ItemStack PORTABLE_BACKPACK = ItemStackBuilder.rebar(Material.SEA_LANTERN, DoraMachinesKeys.PORTABLE_BACKPACK)
                .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    public static final ItemStack REDSTONE_LINK = ItemStackBuilder.rebar(Material.OAK_BUTTON, DoraMachinesKeys.REDSTONE_LINK)
            .build();
    public static final ItemStack LINKED_CONNECTOR = ItemStackBuilder.rebar(Material.CLAY_BALL, DoraMachinesKeys.LINKED_CONNECTOR)
                .set(DataComponentTypes.ITEM_MODEL, Material.MUSIC_DISC_LAVA_CHICKEN.getKey())
                .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    public static final ItemStack HYDRAULIC_WOODCUTTER = ItemStackBuilder.rebar(Material.BRICKS, DoraMachinesKeys.HYDRAULIC_WOODCUTTER)
            .build();
    public static final ItemStack DIESEL_WOODCUTTER = ItemStackBuilder.rebar(Material.RESIN_BRICKS, DoraMachinesKeys.DIESEL_WOODCUTTER)
            .build();
    public static final ItemStack REDSTONE_CLOCK = ItemStackBuilder.rebar(Material.RED_CONCRETE, DoraMachinesKeys.REDSTONE_CLOCK)
            .build();
    public static final ItemStack VOID_PICKER = ItemStackBuilder.rebar(Material.CLAY_BALL, DoraMachinesKeys.VOID_PICKER)
                .set(DataComponentTypes.ITEM_MODEL, Material.BLACK_BUNDLE.getKey())
                .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    public static final ItemStack SLINGSHOT = ItemStackBuilder.rebar(Material.CROSSBOW, DoraMachinesKeys.SLINGSHOT)
                .set(DataComponentTypes.ITEM_MODEL, Material.STICK.getKey())
                .unset(DataComponentTypes.ENCHANTABLE)
                .unset(DataComponentTypes.MAX_DAMAGE)
            .build();
    public static final ItemStack RUCKUS_MAKER = ItemStackBuilder.rebar(Material.CLAY_BALL, DoraMachinesKeys.RUCKUS_MAKER)
                .set(DataComponentTypes.ITEM_MODEL, Material.MUSIC_DISC_11.getKey())
                .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    public static final ItemStack DAGGER = ItemStackBuilder.rebar(Material.WOODEN_SWORD, DoraMachinesKeys.DAGGER)
            .build();

    public static final ItemStack TEMP_BLOCK = ItemStackBuilder.rebar(Material.BLUE_WOOL, DoraMachinesKeys.TEMP_BLOCK)
            .build();
    public static void initialize() {
        RebarItem.register(ExplosionPickaxe.class, EXPLOSION_PICKAXE);
        DoraMachinesPages.TOOL.addItem(EXPLOSION_PICKAXE);
        RebarItem.register(ExplosionShovel.class, EXPLOSION_SHOVEL);
        DoraMachinesPages.TOOL.addItem(EXPLOSION_SHOVEL);
        RebarItem.register(UnbreakableRune.class, UNBREAKABLE_RUNE);
        DoraMachinesPages.TOOL.addItem(UNBREAKABLE_RUNE);
        RebarItem.register(GoldenBoneMeal.class, GOLDEN_BONE_MEAL);
        DoraMachinesPages.TOOL.addItem(GOLDEN_BONE_MEAL);
        RebarItem.register(Filter.class, FILTER);
        DoraMachinesPages.TOOL.addItem(FILTER);
        RebarItem.register(PlanC.class, PLAN_C);
        RebarItem.register(RoadTool.class, ROAD_TOOL);
        DoraMachinesPages.TOOL.addItem(ROAD_TOOL);
        RebarItem.register(PortableBackPack.Item.class, PORTABLE_BACKPACK, DoraMachinesKeys.PORTABLE_BACKPACK);
        DoraMachinesPages.TOOL.addItem(PORTABLE_BACKPACK);
        RebarItem.register(LinkedConnector.class, LINKED_CONNECTOR, DoraMachinesKeys.LINKED_CONNECTOR);
        DoraMachinesPages.TOOL.addItem(LINKED_CONNECTOR);
        RebarItem.register(VoidPicker.class, VOID_PICKER, DoraMachinesKeys.VOID_PICKER);
        DoraMachinesPages.TOOL.addItem(VOID_PICKER);
        RebarItem.register(Slingshot.class, SLINGSHOT, DoraMachinesKeys.SLINGSHOT);
        DoraMachinesPages.TOY.addItem(SLINGSHOT);
        RebarItem.register(RuckusMaker.class, RUCKUS_MAKER, DoraMachinesKeys.RUCKUS_MAKER);
        DoraMachinesPages.TOY.addItem(RUCKUS_MAKER);
        RebarItem.register(Dagger.class, DAGGER, DoraMachinesKeys.DAGGER);
        //DoraMachinesPages.TOY.addItem(DAGGER);
        
        RebarItem.register(RebarItem.class, INDUSTRIAL_MINER, DoraMachinesKeys.INDUSTRIAL_MINER);
        DoraMachinesPages.BLOCK.addItem(INDUSTRIAL_MINER);
        RebarItem.register(RebarItem.class, FILTER_BASE, DoraMachinesKeys.FILTER_BASE);
        DoraMachinesPages.BLOCK.addItem(FILTER_BASE);
        RebarGuide.getOrCreateInfoPage(DoraMachinesKeys.FILTER_BASE)
                .addButton(FilterPage.getButton());
        RebarItem.register(RebarItem.class, MULTI_FLUID_TANK, DoraMachinesKeys.MULTI_FLUID_TANK);
        DoraMachinesPages.BLOCK.addItem(MULTI_FLUID_TANK);
        RebarItem.register(RebarItem.class, TIMING_CHARGER, DoraMachinesKeys.TIMING_CHARGER);
        DoraMachinesPages.BLOCK.addItem(TIMING_CHARGER);
        RebarItem.register(LiseletteEnchanter.Item.class, LISELETTE_ENCHANTER, DoraMachinesKeys.LISELETTE_ENCHANTER);
        DoraMachinesPages.BLOCK.addItem(LISELETTE_ENCHANTER);
        RebarItem.register(LiseletteDisenchanter.Item.class, LISELETTE_DISENCHANTER, DoraMachinesKeys.LISELETTE_DISENCHANTER);
        DoraMachinesPages.BLOCK.addItem(LISELETTE_DISENCHANTER);
        RebarItem.register(RedstoneLink.Item.class, REDSTONE_LINK, DoraMachinesKeys.REDSTONE_LINK);
        DoraMachinesPages.BLOCK.addItem(REDSTONE_LINK);
        RebarItem.register(HydraulicWoodcutter.Item.class, HYDRAULIC_WOODCUTTER, DoraMachinesKeys.HYDRAULIC_WOODCUTTER);
        DoraMachinesPages.BLOCK.addItem(HYDRAULIC_WOODCUTTER);
        RebarItem.register(DieselWoodcutter.Item.class, DIESEL_WOODCUTTER, DoraMachinesKeys.DIESEL_WOODCUTTER);
        DoraMachinesPages.BLOCK.addItem(DIESEL_WOODCUTTER);
        RebarItem.register(RebarItem.class, REDSTONE_CLOCK, DoraMachinesKeys.REDSTONE_CLOCK);
        DoraMachinesPages.BLOCK.addItem(REDSTONE_CLOCK);

        RebarItem.register(RebarItem.class, TEMP_BLOCK, DoraMachinesKeys.TEMP_BLOCK);
    }
}
