package io.github.tagdl.DoraMachines.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.InventoryTickerRebarItem;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.tagdl.DoraMachines.DoraMachines;
import net.kyori.adventure.text.Component;

public class AssaultShield extends RebarItem implements
        InteractRebarItemHandler,
        InventoryTickerRebarItem
{
    private static final NamespacedKey ATTACK_KEY = new NamespacedKey(DoraMachines.getInstance(), "shield_attack");
    private static final NamespacedKey MODE_KEY = new NamespacedKey(DoraMachines.getInstance(), "shield_mode");
    private final int DAMAGE = getSettingOrThrow("damage", ConfigAdapter.INTEGER);
    private final int COOLDOWN = getSettingOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    private final int DISTANCE = getSettingOrThrow("affect-distance", ConfigAdapter.INTEGER);
    private final int ANGLE = getSettingOrThrow("affect-angle", ConfigAdapter.INTEGER);
    public AssaultShield(@NotNull ItemStack stack) {
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
        if (!(RebarItem.fromStack(player.getInventory().getItemInMainHand()) instanceof AssaultShield)) return;
        if (player.isSneaking()) {
            player.sendMessage(Component.translatable("doramachines.message.assault_shield." + (getMode() ? "assault" : "push")));
            setMode(!getMode());
            event.setCancelled(true);
        } else if (!getMode()) {
            player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.1));
            player.setCooldown(getStack(), COOLDOWN);
            setAttack(player, true);
            for (int i = 0; i < 360; i += 20) 
                player.getWorld().spawnParticle(
                    Particle.CLOUD, 
                    player.getLocation().add(new Vector(Math.cos(Math.toRadians(i)) * 2, Math.sin(Math.toRadians(i)) * 2, 0.8)
                        .rotateAroundX(Math.toRadians(player.getEyeLocation().getPitch()))
                        .rotateAroundY(Math.toRadians(-player.getEyeLocation().getYaw()))), 
                    0);
            Bukkit.getScheduler().runTaskLater(DoraMachines.getInstance(), () -> {
                if (player.getPersistentDataContainer().has(ATTACK_KEY)) player.getPersistentDataContainer().remove(ATTACK_KEY);
            }, COOLDOWN + 1);
        }
    }
    public boolean getMode(){ //false is asault_shield
        return getStack().getPersistentDataContainer().getOrDefault(MODE_KEY, RebarSerializers.BOOLEAN, false);
    }
    public void setMode(boolean status){ //false is asault_shield
        getStack().editPersistentDataContainer(pdc -> pdc.set(MODE_KEY, RebarSerializers.BOOLEAN, status));
    }
    public static boolean getAttack(Player player){
        return player.getPersistentDataContainer().getOrDefault(ATTACK_KEY, RebarSerializers.BOOLEAN, false);
    }
    public static void setAttack(Player player, boolean status){
        player.getPersistentDataContainer().set(ATTACK_KEY, RebarSerializers.BOOLEAN, status);
    }
    public static List<LivingEntity> getFrontEntity(Player player, int distance, int angle) {
        List<LivingEntity> result = new ArrayList<>();
        for (Entity raw_entity : player.getNearbyEntities(distance, distance, distance)) {
            if (raw_entity == player) continue;
            if (!(raw_entity instanceof LivingEntity entity)) continue;
            Vector vec = entity.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();
            if (player.getEyeLocation().getDirection().angle(vec) <= Math.toRadians(angle)) result.add(entity);
        }
        return result;
    }
    @Override
    public void onTick(@NotNull Player player) {
        if (!getMode() ? !getAttack(player) : !player.isBlocking()) return;
        if (!(RebarItem.fromStack(player.getInventory().getItemInMainHand()) instanceof AssaultShield shield)) return;
        for (LivingEntity entity : getFrontEntity(player, DISTANCE, ANGLE)) {
            entity.setVelocity(!getMode() ? player.getLocation().getDirection().multiply(3).setY(0.3)
                : player.getEyeLocation().getDirection().multiply(player.getVelocity().length() * 3).setY(0.1));
            if (!getMode()) {
                int damage = DAMAGE;
                if (shield.getStack().getEnchantments().containsKey(Enchantment.CHANNELING)) {
                    entity.getWorld().strikeLightningEffect(entity.getLocation());
                    damage += 5;
                }
                entity.damage(damage, player);
                if (shield.getStack().getEnchantments().containsKey(Enchantment.FIRE_ASPECT)) entity.setFireTicks(shield.getStack().getEnchantmentLevel(Enchantment.FIRE_ASPECT) * 80);
            }
            if (player.getGameMode() != GameMode.CREATIVE) RebarUtils.damageItem(shield.getStack(), 1, player.getWorld());
        }
        if (player.getPersistentDataContainer().has(ATTACK_KEY)) player.getPersistentDataContainer().remove(ATTACK_KEY);
    }
    @Override
    public long getBaseTickInterval() {
        return 1;
    }
    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("mode",
            Component.translatable("doramachines.item.assault_shield." + (!getMode() ? "assault.name" : "push.name"))),
            RebarArgument.of("dist", Component.translatable("doramachines.item.assault_shield." + (!getMode() ? "assault.lore" : "push.lore")))
        );
    }
    public static class AnvilEnchant implements Listener {
        @EventHandler
        public void onPlayerLand(PrepareAnvilEvent event) {
            ItemStack book = event.getInventory().getSecondItem();
            ItemStack raw_shield = event.getResult();
            if (raw_shield == null || raw_shield.isEmpty()) 
                if (event.getInventory().getFirstItem() == null || event.getInventory().getFirstItem().isEmpty()) return;
                else raw_shield = event.getInventory().getFirstItem();
            if (!(RebarItem.fromStack(raw_shield.clone()) instanceof AssaultShield shield)) return;
            if (book == null || book.isEmpty()) return;
            if (book.getType() != Material.ENCHANTED_BOOK) return;
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();
            int cost = 0;
            for (Enchantment enchantment : esm.getStoredEnchants().keySet()) {
                int level = esm.getStoredEnchantLevel(enchantment);
                if (enchantment == Enchantment.CHANNELING || enchantment == Enchantment.FIRE_ASPECT) {
                    shield.getStack().addUnsafeEnchantment(enchantment, level);
                    cost += 3;
                }
            }
            event.getView().setRepairCost(event.getView().getRepairCost() + cost);
            event.setResult(shield.getStack());
        }
    }
}
