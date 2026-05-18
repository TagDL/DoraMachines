package io.github.username.exampleaddon.blocks;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class ExampleBlock extends RebarBlock implements RebarInteractBlock {

    public final int smokeCount = getSettings().getOrThrow("smoke-count", ConfigAdapter.INTEGER);

    // 'Place' constructor - called when the block is placed down
    public ExampleBlock(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    // 'Load' constructor - called when the block is loaded
    public ExampleBlock(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(0, 0.7, 0))
                .count(smokeCount)
                .extra(0.1) // speed
                .spawn();
    }
}
