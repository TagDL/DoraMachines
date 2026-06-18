package io.github.tagdl.DoraMachines.items;

import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class UnbreakableRune extends Rune {
    public static final Component SUCCESS = Component.translatable("doramachines.message.unbreakable_result.success");

    public UnbreakableRune(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        return !target.getItemMeta().isUnbreakable();
    }

    @Override
    public void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        int consume = Math.min(rune.getAmount(), target.getAmount());

        Player player = event.getPlayer();
        ItemStack handle = ItemStackBuilder.of(target.asQuantity(consume))
                .set(DataComponentTypes.UNBREAKABLE)
                .build();

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

        target.setAmount(0);
        rune.setAmount(0);
        player.sendMessage(SUCCESS);
    }
}
