package io.github.tagdl.DoraMachines.items;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;

public class Filter extends RebarItem implements InteractRebarItemHandler {
    public Filter(@NotNull ItemStack stack) {
        super(stack);
    }
    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority){
        if (event.getAction().isLeftClick()) return;
        event.setCancelled(true);
    }
}