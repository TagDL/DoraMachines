package io.github.tagdl.DoraMachines.items;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import com.destroystokyo.paper.ParticleBuilder;

import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.content.fluid.FluidPipe;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.event.PreRebarBlockPlaceEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.item.interfaces.EntityInteractRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.world.InteractionHand;

public class Slingshot extends RebarItem implements InteractRebarItemHandler, EntityInteractRebarItemHandler{
    public Slingshot(@NotNull ItemStack stack) {
        super(stack);
    }
    private static final NamespacedKey MOB_KEY = new NamespacedKey(DoraMachines.getInstance(), "slingshot_mob");
    private final boolean CAN_STORE_MOB = getSettings().getOrThrow("can-store-mob", ConfigAdapter.BOOLEAN);
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) return;

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }
        if (!(getStack().getItemMeta() instanceof CrossbowMeta meta)) return;
        if (meta.hasChargedProjectiles()) return;
        if (event.getHand() != EquipmentSlot.HAND) {
            event.setCancelled(true);
            return;
        }
        ItemStack itemStack = event.getPlayer().getInventory().getItemInOffHand();
        if (itemStack == null || itemStack.isEmpty()) return;
        net.minecraft.world.entity.player.Player nmsPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        nmsPlayer.startUsingItem(InteractionHand.MAIN_HAND, true);
    }
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteractWithEntity(@NotNull PlayerInteractAtEntityEvent event, @NotNull EventPriority priority) {
        if (!CAN_STORE_MOB) return;
        event.setCancelled(true);
        if (!(getStack().getItemMeta() instanceof CrossbowMeta meta)) return;
        if (meta.hasChargedProjectiles()) return;
        Entity entity = event.getRightClicked();
        if (entity instanceof Player) return;
        if (entity.getType() == EntityType.ENDER_DRAGON) return;
        new ParticleBuilder(Particle.SCULK_SOUL).location(entity.getLocation()).spawn();
        meta.addChargedProjectile(ItemStackBuilder.of(Material.PIG_SPAWN_EGG)
            .name(Component.translatable(entity.getType().translationKey()))
            .editPdc(pdc -> pdc.set(MOB_KEY, RebarSerializers.STRING, entity.getAsString())).build());
        Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> getStack().setItemMeta(meta));
        entity.remove();
    }
    public static final class CrossbowLoad implements Listener {
        @EventHandler
        public void onCrossbowLoad(EntityLoadCrossbowEvent event){
            if (!(event.getEntity() instanceof Player player)) return;
            if (!(RebarItem.fromStack(player.getInventory().getItemInMainHand()) instanceof Slingshot slingshot)) return;
            if (event.getHand() != EquipmentSlot.HAND) {
                event.setCancelled(true);
                return;
            }
            ItemStack itemStack = player.getInventory().getItemInOffHand();
            if (itemStack == null || itemStack.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            itemStack = itemStack.asOne();
            if (!(slingshot.getStack().getItemMeta() instanceof CrossbowMeta meta)) return;
            player.getInventory().getItemInOffHand().subtract();
            meta.setChargedProjectiles(List.of(itemStack));
            slingshot.getStack().setItemMeta(meta);
        }
        @EventHandler
        public void onShoot(EntityShootBowEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!(RebarItem.fromStack(event.getBow()) instanceof Slingshot)) return;
            Entity arrow = event.getProjectile();
            arrow.setInvisible(true);
            if (event.getConsumable().getPersistentDataContainer().has(MOB_KEY)) {
                EntitySnapshot snapshot = Bukkit.getEntityFactory()
                    .createEntitySnapshot(event.getConsumable().getPersistentDataContainer().get(MOB_KEY, RebarSerializers.STRING));
                Entity entity = snapshot.createEntity(event.getProjectile().getLocation());
                event.getConsumable().editPersistentDataContainer(pdc -> pdc.remove(MOB_KEY));
                Bukkit.getScheduler().runTaskTimer(DoraMachines.getInstance(), 
                    task -> run(player, task, arrow, entity), 0, 1); 
            } else {
                ItemDisplay display = new ItemDisplayBuilder()
                    .itemStack(event.getConsumable()).build(event.getProjectile().getLocation());
                Bukkit.getScheduler().runTaskTimer(DoraMachines.getInstance(), 
                    task -> run(player, task, arrow, display, event.getConsumable()), 0, 1); 
            }
            
        }
        private void run(Player player, BukkitTask task, Entity arrow, Entity entity) {
            if (arrow.isDead() || !arrow.getLocation().getChunk().isLoaded() || arrow.isOnGround()) {
                arrow.remove();
                task.cancel();
                return;
            }
            Location location = arrow.getLocation().add(arrow.getLocation().getDirection().normalize().multiply(0.8));
            entity.teleport(location);
        }
        private void run(Player player, BukkitTask task, Entity arrow, ItemDisplay display, ItemStack itemStack) {
            if (arrow.isDead() || !arrow.getLocation().getChunk().isLoaded()) {
                arrow.remove();
                display.remove();
                task.cancel();
                return;
            } else if (arrow.isOnGround()) {
                arrow.remove();
                display.remove();
                if (RebarItem.isRebarItem(itemStack)) {
                    if (RebarItem.fromStack(itemStack).getRebarBlock() == null || RebarItem.fromStack(itemStack).getClass().equals(FluidPipe.class)) {
                        arrow.getLocation().getWorld().dropItem(arrow.getLocation().toCenterLocation(), itemStack);
                    } else {
                        Block block = !arrow.getLocation().getBlock().getType().isSolid()
                            ? arrow.getLocation().getBlock().isReplaceable() 
                                ? arrow.getLocation().getBlock() 
                                : arrow.getLocation().add(arrow.getLocation().getDirection().normalize().multiply(0.6)).getBlock()
                            : arrow.getLocation().add(arrow.getLocation().getDirection().normalize().multiply(0.6)).getBlock();
                        if (block.isReplaceable() && block.getType() != Material.STRUCTURE_VOID) 
                            if (!canRebarPlace(itemStack, block, player)) arrow.getLocation().getWorld().dropItem(arrow.getLocation().toCenterLocation(), itemStack);
                            else RebarItem.fromStack(itemStack).place(new BlockCreateContext.Default(player, block));
                        else arrow.getLocation().getWorld().dropItem(arrow.getLocation().toCenterLocation(), itemStack);
                    } 
                } else {
                    if (itemStack.getType().isBlock()) {
                        Block block = !arrow.getLocation().getBlock().getType().isSolid() 
                            ? arrow.getLocation().getBlock().isReplaceable() 
                                ? arrow.getLocation().getBlock() 
                                : arrow.getLocation().add(arrow.getLocation().getDirection().normalize().multiply(0.6)).getBlock()
                            : arrow.getLocation().add(arrow.getLocation().getDirection().normalize().multiply(0.6)).getBlock();
                        if (block.isReplaceable() && block.getType() != Material.STRUCTURE_VOID && canPlace(itemStack, block, player)) 
                            ((BlockStateMeta) itemStack.getItemMeta()).getBlockState().copy(block.getLocation()).update(true, true);
                        else arrow.getLocation().getWorld().dropItem(arrow.getLocation().toCenterLocation(), itemStack);
                    } else if (arrow.getLocation().getBlock().getType() == Material.FARMLAND && canPlant(itemStack.getType()) != null) {
                        Block block = arrow.getLocation().add(arrow.getLocation().getDirection().normalize().multiply(0.6)).getBlock();
                        if (block.isReplaceable() && block.getType() != Material.STRUCTURE_VOID) block.setType(canPlant(itemStack.getType()));
                        else arrow.getLocation().getWorld().dropItem(arrow.getLocation().toCenterLocation(), itemStack);
                    } else arrow.getLocation().getWorld().dropItem(arrow.getLocation().toCenterLocation(), itemStack);  
                }
                task.cancel();
                return;
            } else display.teleport(arrow);
        }
        private Material canPlant(Material seed) {
            return switch (seed) {
                case WHEAT_SEEDS -> Material.WHEAT;
                case CARROT -> Material.CARROTS;
                case POTATO -> Material.POTATOES;
                case BEETROOT_SEEDS -> Material.BEETROOTS;
                case NETHER_WART -> Material.NETHER_WART;
                default -> null;
            };
        }
        private boolean canPlace(ItemStack itemStack, Block block, Player player) {
            if (player.getGameMode() == GameMode.ADVENTURE) return false;
            BlockState state = block.getState();
            state.setType(itemStack.getType());
            BlockPlaceEvent event = new BlockPlaceEvent(block, state, block, itemStack, player, true, EquipmentSlot.HAND);
            Bukkit.getPluginManager().callEvent(event);
            return event.canBuild() && !event.isCancelled();
        }
        private boolean canRebarPlace(ItemStack itemStack, Block block, Player player) {
            if (player.getGameMode() == GameMode.ADVENTURE) return false;
                PreRebarBlockPlaceEvent event = new PreRebarBlockPlaceEvent(block, 
                    (RebarBlockSchema) RebarRegistry.BLOCKS.get(RebarItem.fromStack(itemStack).getRebarBlock()),
                    new BlockCreateContext.Default(player, block));  
                event.callEvent();   
                return !event.isCancelled();
        }
    }
}
