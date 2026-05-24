package io.github.tagdl.DoraMachines.items;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;

public class PlanC extends RebarItem implements RebarInteractor {
    public PlanC(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority){
        if (event.getHand() != EquipmentSlot.HAND 
                || event.useItemInHand() == Event.Result.DENY
                || event.getAction().isLeftClick()) return;
        Collection<LivingEntity> entities = event.getPlayer().getLocation().getNearbyLivingEntities(5, 5, 5);
        if (entities.isEmpty()) return;
        this.getStack().subtract();
        for (LivingEntity entity : entities) {
            entity.damage(9999.99, event.getPlayer());
        }
        event.setCancelled(true);
    }
}
