package io.github.tagdl.DoraMachines.blocks;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarNoVanillaContainerBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Effect;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndustrialMiner extends RebarBlock implements 
        RebarDirectionalBlock,
        RebarSimpleMultiblock,
        RebarInteractBlock,
        RebarNoVanillaContainerBlock
{   
    public static final Component LAVA_LACK = Component.translatable("doramachines.message.industrial_miner.lava");
    public static final Component FULL = Component.translatable("doramachines.message.industrial_miner.full");
    public static final Component UNCOMPLETE = Component.translatable("doramachines.message.industrial_miner.uncomplete");
    public static final Component BLOCKED = Component.translatable("doramachines.message.industrial_miner.blocked");
    public static final Component RUNNING = Component.translatable("doramachines.message.industrial_miner.running");
    private static final int range = 6;
    private static final int TIMETOTAL = 180;
    private MiningTask task;
    private List<Material> ores = List.of(
        Material.COAL_ORE,
        Material.DEEPSLATE_COAL_ORE,
        Material.COPPER_BLOCK,
        Material.DEEPSLATE_COPPER_ORE,
        Material.IRON_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.GOLD_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.LAPIS_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.REDSTONE_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.NETHER_QUARTZ_ORE,
        Material.NETHER_GOLD_ORE
    );
    @SuppressWarnings("unused")
    public IndustrialMiner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        this.setFacing(context.getFacing());
        this.setMultiblockDirection(context.getFacing());
    }
    @SuppressWarnings("unused")
    public IndustrialMiner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Piston pistonData = (Piston) Material.PISTON.createBlockData();
        pistonData.setFacing(BlockFace.UP);
        BlockData pistonL = List.of(pistonData).get(0);

        Map<Vector3i, MultiblockComponent> map = new HashMap<>();
        map.put(new Vector3i(1, 0, 0), MultiblockComponent.of(Material.DIAMOND_BLOCK));
        map.put(new Vector3i(-1, 0, 0), MultiblockComponent.of(Material.DIAMOND_BLOCK));
        map.put(new Vector3i(0, 1, 0), MultiblockComponent.of(Material.CHEST));
        map.put(new Vector3i(1, 1, 0), MultiblockComponent.of(pistonL));
        map.put(new Vector3i(-1, 1, 0), MultiblockComponent.of(pistonL));
        return map;
    }
    @Override
    public void onInteract(PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND 
                || event.useItemInHand() == Event.Result.DENY
                || !event.getAction().isRightClick()) return;
        if (!isComplete(event.getPlayer())) return;
        if (!(event.getClickedBlock().getRelative(BlockFace.UP)
                .getState(false) instanceof Chest chest)) return;
        Inventory inv = chest.getInventory();
        World world = event.getClickedBlock().getWorld();
        if(!hasempty(inv)) {
            event.getPlayer().sendMessage(FULL);
            return;
        }
        if (!inv.contains(Material.LAVA_BUCKET)) {
            event.getPlayer().sendMessage(LAVA_LACK);
            return;
        }
        if (getblock(world, inv, event.getBlockFace(), 1, 1, 0).getType() != Material.AIR 
                || getblock(world, inv, event.getBlockFace(), -1, 1, 0).getType() != Material.AIR) {
            event.getPlayer().sendMessage(BLOCKED);
            return;
        }
        if (task == null || task.isCancelled()) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getContents()[i];
                if (item == null) continue;
                if (item.getType() == Material.LAVA_BUCKET) {
                    Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                        item.subtract();
                        inv.addItem(new ItemStack(Material.BUCKET));
                    });
                    break;
                }
            }
            task = new MiningTask(
                event.getClickedBlock().getLocation().add(-range , -1 ,-range), 
                event.getClickedBlock().getLocation().add(range , 0, range),
                event.getPlayer(),
                world, inv, event.getBlockFace());
            task.runTaskTimer(DoraMachines.getInstance(), 0L, 1L);
        } else {
            if (task.runnable) {
                event.getPlayer().sendMessage(RUNNING);
            } else {
                task.runnable = true;
                task.spendTime = 0;
            }
        }
    }
    private boolean hasempty(Inventory inv){
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getContents()[i];
            if (item == null) return true;
        }
        return false;
    }
    private boolean isComplete(Player p){
        if (isFormedAndFullyLoaded()) return true;
        p.sendMessage(UNCOMPLETE);
        if (task != null) {
            task.spendTime = 0;
            task.runnable = false;
        }
        return false;
    }
    private Block getblock(World world, Inventory inv, BlockFace facing, double x, double y, double z){
        return world.getBlockAt(inv.getLocation().add(getVector(facing, x, y, z)));
    }
    private Vector getVector(BlockFace facing, double x, double y, double z) {
        switch (facing) {
            case NORTH: return new Vector(x, y, z);
            case SOUTH: return new Vector(-x, y, -z);
            case EAST:  return new Vector(-z, y, x);
            case WEST:  return new Vector(z, y, -x);
            default:    return new Vector(x, y, z);
        }
    }
    private void turnPiston(Block pistonRaw, Block pistonOn, boolean On){
        if (pistonRaw.getType() == Material.AIR) return;
        Piston piston =(Piston) pistonRaw.getBlockData();     
        if (On) {
            
            piston.setExtended(true);
            pistonOn.setType(Material.PISTON_HEAD);
            PistonHead pistonHead = (PistonHead) pistonOn.getBlockData();
            pistonHead.setFacing(BlockFace.UP);
            pistonRaw.setBlockData(piston, false);
            pistonOn.setBlockData(pistonHead, false);
        } else {
            piston.setExtended(false);
            piston.setFacing(BlockFace.UP);
            pistonOn.setType(Material.AIR, false);
            pistonRaw.setBlockData(piston, false);
        }
    }
    private class MiningTask extends BukkitRunnable{
        private final Location start;
        private final Location end;
        private final World world;
        private final Inventory inv;
        private final Player p;
        private final BlockFace facing;
        private double y;
        private double x;
        private double z;
        private int amount = 0;
        private Material oreType = Material.AIR;
        public int spendTime = 0;
        public boolean runnable = true;
        public MiningTask(Location start, Location end, Player p, World world, Inventory inv, BlockFace facing){
            this.start = start;
            this.end = end;
            this.p = p;
            this.world = world;
            this.inv = inv;
            this.facing = facing;
            this.y = this.start.getY();
            this.x = this.start.getX();
            this.z = this.start.getZ();
        }
        @Override
        public void run() {       
            if (!runnable || !getblock(this.world, this.inv, this.facing, 0, 0, 0)
                        .getChunk().isLoaded()) return;
            Block blockL = getblock(this.world, this.inv, this.facing, 1, 0, 0);
            Block blockR = getblock(this.world, this.inv, this.facing, -1, 0, 0);
            Block blockLU = getblock(this.world, this.inv, this.facing, 1, 1, 0);
            Block blockRU = getblock(this.world, this.inv, this.facing, -1, 1, 0);
            if (!isComplete(this.p)) {
                this.cancel();
                return;
            }
            if (getTime() == 0) {
                if (inv.contains(Material.LAVA_BUCKET)) {
                    for (int i = 0; i < this.inv.getSize(); i++) {
                        ItemStack item = this.inv.getContents()[i];
                        if (item == null) continue;
                        if (item.getType() == Material.LAVA_BUCKET) {
                            Bukkit.getScheduler().runTask(DoraMachines.getInstance(), () -> {
                                item.subtract();
                                this.inv.addItem(new ItemStack(Material.BUCKET));
                            });
                            break;
                        }
                    }
                spendTime = 0;
                } else {
                    this.p.sendMessage(LAVA_LACK);
                    turnPiston(blockL, blockLU, false);
                    turnPiston(blockR, blockRU, false);
                    runnable = false;
                    return;
                }
            }
            if (getblock(this.world, this.inv, this.facing, 0, -1, 0).getType() == Material.AIR) {
                this.p.sendMessage(UNCOMPLETE);
                this.cancel();
                return;
            }
            if (blockLU.getType() != Material.AIR
                    || blockRU.getType() != Material.AIR) {
                if (blockLU.getType() != Material.PISTON_HEAD
                        && blockRU.getType() != Material.PISTON_HEAD) {
                    this.p.sendMessage(BLOCKED);
                    this.cancel();
                    return;
                }
            }
            Piston tempPiston = (Piston) blockR.getBlockData();
            if (spendTime % 5 == 0) {
                if (tempPiston.isExtended()) {
                    turnPiston(blockL, blockLU, true);
                    turnPiston(blockR, blockRU, false);
                } else {
                    turnPiston(blockL, blockLU, false);
                    turnPiston(blockR, blockRU, true);
                }
            }

            spendTime++;
            Block block = this.world.getBlockAt(new Location(this.world, this.x, this.y, this.z));
            if (ores.contains(block.getType())) {
                if (inv.contains(block.getType()) || hasempty(inv)) {
                    inv.addItem(new ItemStack(block.getType()));
                    world.playEffect(inv.getLocation(), Effect.STEP_SOUND, block.getType());
                    oreType = block.getType();
                    block.setType(Material.AIR);
                    ++amount;
                } else {
                    this.p.sendMessage(FULL);
                    this.cancel();
                    return;
                }
            }
            if (this.x < this.end.getX()) {
                ++this.x;
            } else {
                this.x = this.start.getX();
                if (this.z < this.end.getZ()) {
                    ++this.z;
                } else {
                    if (this.y <= this.world.getMinHeight()) {
                        if (this.p != null) {
                            this.p.sendMessage(Component.translatable("doramachines.message.industrial_miner.finish")
                            .arguments(RebarArgument.of("amount", amount)));
                        }
                        this.cancel();
                    }
                    this.x = this.start.getX();
                    this.z = this.start.getZ();
                    --this.y;
                }
            }
        }
        @Override
        public void cancel() {
            if (getblock(this.world, this.inv, this.facing, 1, 0, 0).getType() == Material.PISTON) {
                turnPiston(getblock(this.world, this.inv, this.facing, 1, 0, 0), getblock(this.world, this.inv, this.facing, 1, 1, 0), false);
            }
            if (getblock(this.world, this.inv, this.facing, -1, 0, 0).getType() == Material.PISTON) {
                turnPiston(getblock(this.world, this.inv, this.facing, -1, 0, 0), getblock(this.world, this.inv, this.facing, -1, 1, 0), false);
            }
            super.cancel();
        }
        public Component getWhatOre() {
            if (this.oreType == Material.AIR) return Component.empty();
            return new ItemStack(oreType).effectiveName(); 
        }
        public int getTime(){
            int result = (int) TIMETOTAL - (spendTime / 20);
            return result;
        }
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(Component.translatable(
            "doramachines.item.industrial_miner.waila")
                .arguments(
                    RebarArgument.of("contents", 
                        task == null
                            ? Component.translatable("doramachines.waila.industrial_miner.none")
                            : Component.translatable("doramachines.waila.industrial_miner.minewhat")
                                .arguments(RebarArgument.of("ore", task.getWhatOre()))),
                    RebarArgument.of("processing", 
                        Component.translatable("doramachines.waila.industrial_miner.time")
                            .arguments(
                                RebarArgument.of("time", task == null ? TIMETOTAL : task.getTime()),
                                RebarArgument.of("bars", PylonUtils.createProgressBar(
                                    task == null ? 0 : TIMETOTAL - task.getTime(),
                                    TIMETOTAL, 
                                    20, 
                                    TextColor.color(100, 255, 100)
                                ))
                            )
                    )
                )
        );
    }
}