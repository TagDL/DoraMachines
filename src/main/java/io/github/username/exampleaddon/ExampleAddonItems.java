package io.github.username.exampleaddon;

import io.github.pylonmc.pylon.PylonPages;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.username.exampleaddon.items.ExampleItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public final class ExampleAddonItems {

    public static final ItemStack EXAMPLE_ITEM = ItemStackBuilder.rebar(Material.BLAZE_ROD, ExampleAddonKeys.EXAMPLE_ITEM)
            .build();

    public static final ItemStack EXAMPLE_BLOCK = ItemStackBuilder.rebar(Material.IRON_BLOCK, ExampleAddonKeys.EXAMPLE_BLOCK)
            .build();

    public static void initialize() {
        // Register an item using the ExampleItem class
        RebarItem.register(ExampleItem.class, EXAMPLE_ITEM);
        PylonPages.MISCELLANEOUS.addItem(EXAMPLE_ITEM);

        // Register a 'normal' item which represents Example Block
        // Blocks and their corresponding item will almost always share the same key
        // Note the 3rd parameter - this is the key of the corresponding block registered in [ExampleAddonBlocks]
        RebarItem.register(RebarItem.class, EXAMPLE_BLOCK, ExampleAddonKeys.EXAMPLE_BLOCK);
        PylonPages.MISCELLANEOUS.addItem(EXAMPLE_BLOCK);
    }
}
