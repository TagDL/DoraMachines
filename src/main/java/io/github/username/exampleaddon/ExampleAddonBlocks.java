package io.github.username.exampleaddon;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.username.exampleaddon.blocks.ExampleBlock;
import org.bukkit.Material;


public final class ExampleAddonBlocks {

    public static void initialize() {
        RebarBlock.register(ExampleAddonKeys.EXAMPLE_BLOCK, Material.IRON_BLOCK, ExampleBlock.class);
    }
}
