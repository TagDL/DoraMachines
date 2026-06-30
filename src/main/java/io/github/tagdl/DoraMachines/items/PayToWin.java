package io.github.tagdl.DoraMachines.items;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.destroystokyo.paper.ParticleBuilder;

import io.github.pylonmc.pylon.content.talismans.Talisman;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.tagdl.DoraMachines.DoraMachines;

public class PayToWin extends Talisman {
    public final int PAY_AMOUNT = getSettingOrThrow("cost-gold-ingot-amount", ConfigAdapter.INTEGER);
    private static final NamespacedKey PAY_TO_WIN_KEY = new NamespacedKey(DoraMachines.getInstance(), "pay_to_win_key");
    private static final NamespacedKey PAY_KEY = new NamespacedKey(DoraMachines.getInstance(), "pay_key");
    public PayToWin(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override
    public NamespacedKey getTalismanKey() {
        return PAY_TO_WIN_KEY;
    }
    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(PAY_KEY, RebarSerializers.INTEGER, PAY_AMOUNT);
    }
    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(PAY_KEY);
    }
    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("amount", PAY_AMOUNT));
    }
    public static class PayToWinListener implements Listener {
        @EventHandler @MultiHandler(priorities = EventPriority.LOWEST)
        public void onAttack(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) return;
            if (!player.getPersistentDataContainer().has(PAY_KEY)) return;
            int cost = player.getPersistentDataContainer().get(PAY_KEY, RebarSerializers.INTEGER);
            if (!player.getInventory().containsAtLeast(ItemStack.of(Material.GOLD_INGOT), cost)) return;
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (cost <= 0) break;
                if (itemStack == null || itemStack.isEmpty()) continue;
                if (!itemStack.isSimilar(ItemStack.of(Material.GOLD_INGOT))) continue;
                int consume = Math.min(cost, itemStack.getAmount());
                itemStack.subtract(consume);
                cost -= consume;
            } 
            new ParticleBuilder(Particle.ITEM).count(5).extra(0.35).offset(0.2, 0.2, 0.2)
                .location(event.getEntity().getLocation().add(0, 1, 0)).data(ItemStack.of(Material.GOLD_INGOT)).spawn();
            event.setDamage(event.getDamage() + cost);
        }
    }
}