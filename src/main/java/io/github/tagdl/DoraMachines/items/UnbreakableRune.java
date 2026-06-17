package io.github.tagdl.DoraMachines.items;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author balugaq 因为是他写的我就不删了qwq
 */
@SuppressWarnings("UnstableApiUsage")
public class UnbreakableRune extends Rune {
    public static final Component SUCCESS = Component.translatable("doramachines.message.unbreakable_result.success");

    public UnbreakableRune(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Checks if the rune is applicable to the target item.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     * @return true if applicable, false otherwise
     */
    @Override
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        return !target.getItemMeta().isUnbreakable();
    }

    /**
     * Handles contacting between an item and a rune.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     */
    @Override
    public void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        // As many runes as possible to consume
        int consume = Math.min(rune.getAmount(), target.getAmount());

        Player player = event.getPlayer();
        ItemStack handle = ItemStackBuilder.of(target.asQuantity(consume)) // Already cloned in `asQuantity`
                .set(DataComponentTypes.UNBREAKABLE)
                .build();

        // (N)Either left runes or targets
        int leftRunes = rune.getAmount() - consume;
        int leftTargets = target.getAmount() - consume;

        Location explodeLoc = event.getItemDrop().getLocation();
        World world = explodeLoc.getWorld();
        if (leftRunes > 0) {
            world.dropItemNaturally(explodeLoc, rune.asQuantity(leftRunes)).setGlowing(true);
        }
        if (leftTargets > 0) {
            world.dropItemNaturally(explodeLoc, target.asQuantity(leftTargets)).setGlowing(true);
        }
        world.dropItemNaturally(explodeLoc, handle).setGlowing(true);

        // simple particles
        spawnParticle(Particle.EXPLOSION, explodeLoc, 1);
        spawnParticle(Particle.FLAME, explodeLoc, 50);
        spawnParticle(Particle.SMOKE, explodeLoc, 40);

        target.setAmount(0);
        rune.setAmount(0);
        player.sendMessage(SUCCESS);
    }

    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {
        new ParticleBuilder(particle)
                .location(location)
                .offset(0, 0, 0)
                .count(count)
                .spawn();
    }
}
