package io.github.tagdl.DoraMachines.items;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.tagdl.DoraMachines.DoraMachines;
import net.kyori.adventure.text.Component;

public class Dagger extends RebarItem implements InteractRebarItemHandler{
    private static final NamespacedKey STATUS_KEY = new NamespacedKey(DoraMachines.getInstance(), "status");
    private static final NamespacedKey ATTACK_KEY = new NamespacedKey(DoraMachines.getInstance(), "attack");
    private final int COOLDOWN = getSettingOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public Dagger(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) return;
        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            player.sendMessage(Component.translatable("doramachines.message.dagger." + (!getStatus() ? "enabled" : "disabled")));
            setStatus(!getStatus());
        } else if (getStatus() && (event.getClickedBlock() == null || event.getClickedBlock().isEmpty())) {
            player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.4));
            player.setCooldown(getStack(), COOLDOWN);
            setAttack(player, true);
        }
        Bukkit.getScheduler().runTaskLater(DoraMachines.getInstance(), ()->{
            if (player.getPersistentDataContainer().has(ATTACK_KEY)) player.getPersistentDataContainer().remove(ATTACK_KEY);
        }, COOLDOWN + 1);
    }
    public boolean getStatus(){
        return getStack().getPersistentDataContainer().getOrDefault(STATUS_KEY, RebarSerializers.BOOLEAN, false);
    }
    public void setStatus(boolean status){
        getStack().editPersistentDataContainer(pdc -> pdc.set(STATUS_KEY, RebarSerializers.BOOLEAN, status));
    }
    public static boolean getAttack(Player player){
        return player.getPersistentDataContainer().getOrDefault(ATTACK_KEY, RebarSerializers.BOOLEAN, false);
    }
    public static void setAttack(Player player, boolean status){
        player.getPersistentDataContainer().set(ATTACK_KEY, RebarSerializers.BOOLEAN, status);
    }
    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("enabled",
                Component.translatable("doramachines.item.dagger." + (getStatus() ? "enabled" : "disabled"))));
    }
    public static final class PlayerAttack implements Listener {
        @EventHandler
        public void onPlayerLand(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) return;
            if (!getAttack(player)) return;
            if (!(RebarItem.fromStack(player.getInventory().getItemInMainHand()) instanceof Dagger dagger)) return;
            if (!dagger.getStatus()) return;
            if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;
            player.getPersistentDataContainer().remove(ATTACK_KEY);
            player.setVelocity(player.getLocation().getDirection().multiply(-1.5).setY(0.4));
        }
    }
}
