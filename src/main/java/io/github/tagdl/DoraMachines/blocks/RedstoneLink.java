package io.github.tagdl.DoraMachines.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import io.github.lijinhong11.rebarwrench.api.WrenchAction;
import io.github.lijinhong11.rebarwrench.api.WrenchResult;
import io.github.lijinhong11.rebarwrench.api.Wrenchable;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.github.tagdl.DoraMachines.DoraMachines;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import net.kyori.adventure.text.Component;

public class RedstoneLink extends RebarBlock implements
    DirectionalRebarBlock,
    TickingRebarBlock,
    EntityHolderRebarBlock,
    InteractRebarBlockHandler
{
    private static ConfigSection settings = ConfigSection.fromSettings(DoraMachinesKeys.REDSTONE_LINK);
    private static int radius = settings.getOrThrow("radius", ConfigAdapter.INTEGER);
    private static int ticks = settings.getOrThrow("detect-ticks", ConfigAdapter.INTEGER);
    private static final NamespacedKey LINK_TYPE = new NamespacedKey(DoraMachines.getInstance(), "link_type");
    private static final NamespacedKey UUID_TYPE = new NamespacedKey(DoraMachines.getInstance(), "uuid_type");
    private List<RedstoneLink> blocks = new ArrayList<>();
    private boolean link_type = false; //false is send redstone signal
    private UUID receive_uuid;
    public static final Wrenchable WRENCHABLE = Wrenchable.builder()
            .interactFunction(RedstoneLink::onWrench)
            .build(); //碎碎念：有点为了这醋包一碟饺子了，算了用吧用吧 :P
    private final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.BARREL)
        .addCustomModelDataString(getKey() + ":main");
    private final ItemStackBuilder receiveStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CARPET)
        .addCustomModelDataString(getKey() + ":receive");

    public static class Item extends RebarItem {
        private static ConfigSection settings = ConfigSection.fromSettings(DoraMachinesKeys.REDSTONE_LINK);
        private static int radius = settings.getOrThrow("radius", ConfigAdapter.INTEGER);
        private static int ticks = settings.getOrThrow("detect-ticks", ConfigAdapter.INTEGER);
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }
        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("radius", radius),
                    RebarArgument.of("ticks", ticks),
                    RebarArgument.of("have-wrench", Bukkit.getPluginManager().getPlugin("RebarWrench") == null 
                            || !Bukkit.getPluginManager().isPluginEnabled("RebarWrench") 
                        ? Component.translatable("doramachines.item.redstone_link.have_wrench")
                        : Component.translatable("doramachines.item.redstone_link.dont_have_wrench"))
            );
        }
    }
    public static WrenchResult onWrench(Player player, RebarBlock block, WrenchAction action) {
        RedstoneLink redstoneLink = (RedstoneLink) block;
        if (!action.equals(WrenchAction.CONFIGURE)) return WrenchResult.SUCCESS;
        redstoneLink.link_type = !redstoneLink.link_type;
        player.sendActionBar(!redstoneLink.link_type
            ? Component.translatable("doramachines.message.redstone_link.transmitter")
            : Component.translatable("doramachines.message.redstone_link.receiver")
        );
        return WrenchResult.SUCCESS;
    }
    @SuppressWarnings("unused")
    public RedstoneLink(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(ticks);
        this.setFacing(context.getFacing());
        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getWhatFace())
                        .translate(0, 0, -0.46)
                        .rotate(Math.PI / 2, 0, 0)
                        .scale(0.8, 0.2, 0.8)
                )
                .build(block.getLocation().toCenterLocation())
        );
        addEntity("torch", new BlockDisplayBuilder()
                .blockData(Bukkit.createBlockData("minecraft:redstone_torch[lit=false]"))
                .transformation(new TransformBuilder()
                        .lookAlong(getWhatFace())
                        .translate(-0.3, 0.3, -0.2)
                        .scale(0.5)
                        .rotate(Math.PI / 2, 0, 0)
                )
                .build(block.getLocation().toCenterLocation())
        );
        addEntity("receive", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .lookAlong(getWhatFace())
                        .translate(-0.3, 0.3, -0.15)
                        .scale(0.2)
                        .rotate(Math.PI / 2, 0, 0)
                )
                .build(block.getLocation().toCenterLocation())
        );
        // addEntity("channelup", new ItemDisplayBuilder()
        //         .transformation(new TransformBuilder()
        //                 .lookAlong(getWhatFace())
        //                 .translate(0, 0.2, -0.43)
        //                 .scale(0.2)
        //         )
        //         .build(block.getLocation().toCenterLocation())
        // );
        // addEntity("channeldown", new ItemDisplayBuilder()
        //         .transformation(new TransformBuilder()
        //                 .lookAlong(getWhatFace())
        //                 .translate(0, -0.2, -0.43)
        //                 .scale(0.2)
        //         )
        //         .build(block.getLocation().toCenterLocation())
        // ); //这是机械动力的信道逻辑，保留下来给有需要的人
        receive_uuid = receive_uuid == null ? getHeldEntity(ItemDisplay.class, "main").getUniqueId() : receive_uuid;
    }
    @SuppressWarnings("unused")
    public RedstoneLink(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        link_type = pdc.getOrDefault(LINK_TYPE, RebarSerializers.BOOLEAN, false);
        receive_uuid = pdc.getOrDefault(UUID_TYPE, RebarSerializers.UUID, null);
    }
    @Override
    public void tick() {
        if (getBlock() == null || !getBlock().getChunk().isLoaded()) return;
        getHeldEntity(ItemDisplay.class, "receive").setItemStack(link_type ? receiveStack.build() : null);
        if (!link_type) { //sender logic
            getHeldEntity(BlockDisplay.class, "torch")
                .setBlock(Bukkit.createBlockData("minecraft:redstone_torch[lit=" + isPowered() + "]"));
            if (getBlock().getBlockData() instanceof Switch switch1) {
                switch1.setPowered(false);
                getBlock().setBlockData(switch1, true);
            }
            return;
        }
        //receiver logic
        blocks = findBlocks(false);
        getHeldEntity(BlockDisplay.class, "torch")
                .setBlock(Bukkit.createBlockData("minecraft:redstone_torch[lit=" + !blocks.isEmpty() + "]"));
        if (getBlock().getBlockData() instanceof Switch switch1) {
            switch1.setPowered(!blocks.isEmpty());
            getBlock().setBlockData(switch1, true);
        }
    }
    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(LINK_TYPE, RebarSerializers.BOOLEAN, link_type);
        pdc.set(UUID_TYPE, RebarSerializers.UUID, receive_uuid);
    }
    @Override
    public void onInteractedWith(PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND
                || !event.getAction().isRightClick()
        ) return;
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        // if (event.getItem() == null) { //clean channel //这是机械动力的信道逻辑，保留下来给有需要的人
        //     channelSwitch(null, event.getPlayer().isSneaking());
        // } else if (!event.getItem().getType().equals(Material.STICK)) { //set channel
        //     if (RebarItem.fromStack(event.getItem()) instanceof LinkedController) return;
        //     channelSwitch(event.getItem().asOne(), event.getPlayer().isSneaking());
        // } else { //TODO use wrench to switch send/receive after finish
        //     link_type = !link_type;
        //     getHeldEntity(ItemDisplay.class,"receive").setItemStack(link_type ? receiveStack.build() : null);
        // }
        // if (event.getItem().getType().equals(Material.STICK)) {
        //     link_type = !link_type;
        //     getHeldEntity(ItemDisplay.class,"receive").setItemStack(link_type ? receiveStack.build() : null);
        // }
        if (Bukkit.getPluginManager().getPlugin("RebarWrench") == null 
                || !Bukkit.getPluginManager().isPluginEnabled("RebarWrench")) return;
        if (event.getItem() == null || !event.getItem().getType().equals(Material.STICK)) return;
        link_type = !link_type;
        event.getPlayer().sendActionBar(!link_type
            ? Component.translatable("doramachines.message.redstone_link.transmitter")
            : Component.translatable("doramachines.message.redstone_link.receiver")
        );
    }
    public BlockFace getWhatFace() {
        Switch switch1 = (Switch) getBlock().getBlockData();
        AttachedFace face = switch1.getAttachedFace();
        BlockFace result = switch1.getFacing();
        if (face == AttachedFace.FLOOR) result = BlockFace.UP;
        else if (face == AttachedFace.CEILING) result = BlockFace.DOWN;
        return result;
    }
    // public void channelSwitch(ItemStack itemStack, boolean sneaking) { //这是机械动力的信道逻辑，保留下来给有需要的人
    //     if (!sneaking) getHeldEntity(ItemDisplay.class, "channelup").setItemStack(itemStack);
    //     else getHeldEntity(ItemDisplay.class, "channeldown").setItemStack(itemStack);
    // }
    public boolean getLinkType() {
        return link_type;
    }
    public void setUUID(UUID uuid) {
        receive_uuid = uuid;
    }
    public UUID getUUID() { //if sender then send self uuid else send receive_uuid
        return link_type ? receive_uuid : getHeldEntity(ItemDisplay.class, "main").getUniqueId();
    }
    public List<RedstoneLink> findBlocks(boolean sender) { //find charged, and required same channel
        List<RedstoneLink> foundBlocks = new ArrayList<>();
        Location center = getBlock().getLocation().toBlockLocation();
        BlockStorage.getByKey(DoraMachinesKeys.REDSTONE_LINK).forEach(block -> {
            RedstoneLink redstoneLink = (RedstoneLink) block;
            Location other = redstoneLink.getBlock().getLocation().toBlockLocation();
            int x = center.getBlockX() - other.getBlockX();
            int y = center.getBlockY() - other.getBlockY();
            int z = center.getBlockZ() - other.getBlockZ();
            int radiusSq = radius * radius;
            int distance = x * x + y * y + z * z;
            //false is transmitter, else receiver
            if ((!sender ? !redstoneLink.getLinkType() : redstoneLink.getLinkType()) && redstoneLink.isPowered() 
                && distance < radiusSq && redstoneLink.sameChannel(this)) foundBlocks.add(redstoneLink);
        });
        return foundBlocks;
    }
    public boolean sameChannel(RedstoneLink compairedRedstoneLink) {
        return compairedRedstoneLink.getUUID().equals(getUUID());
    }
    public boolean isPowered() {
        BlockFace[] FACES = {
                BlockFace.NORTH, BlockFace.SOUTH, 
                BlockFace.EAST, BlockFace.WEST, 
                BlockFace.UP, BlockFace.DOWN
            };
        for (BlockFace face : FACES) {
            Block neighbor = this.getBlock().getRelative(face);
            BlockData data = neighbor.getBlockData();
            if (data instanceof RedstoneWire wire) {
                if (wire.getFace(face.getOppositeFace()) != RedstoneWire.Connection.NONE
                        && wire.getPower() > 0) return true;
                if (wire.getPower() > 0 && isDotWire(wire)) return true;
            }
            if (data instanceof Powerable powerable) return powerable.isPowered();
            if (neighbor.getType().equals(Material.REDSTONE_TORCH)) {
                if (data instanceof Lightable lightable) return lightable.isLit();
            }
            if (data instanceof RedstoneWallTorch redstoneWallTorch) return redstoneWallTorch.isLit();
            if (neighbor.getType().isSolid() && neighbor.getBlockPower() > 0) return true;
            if (neighbor.getType() == Material.REDSTONE_BLOCK) return true;
        }
        return false;
    }
    public boolean isDotWire(RedstoneWire wire) {
        return wire.getFace(BlockFace.NORTH) == RedstoneWire.Connection.NONE
            && wire.getFace(BlockFace.SOUTH) == RedstoneWire.Connection.NONE
            && wire.getFace(BlockFace.EAST) == RedstoneWire.Connection.NONE
            && wire.getFace(BlockFace.WEST) == RedstoneWire.Connection.NONE;
    }
    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(Component.translatable("doramachines.item.redstone_link.waila")
                .arguments(RebarArgument.of("active", !link_type 
                    ? isPowered() ? Component.translatable("doramachines.waila.redstone_link.powered")
                        : Component.translatable("doramachines.waila.redstone_link.not_powered")
                    : !blocks.isEmpty() ? Component.translatable("doramachines.waila.redstone_link.received")
                        : Component.translatable("doramachines.waila.redstone_link.not_received")),
                    RebarArgument.of("mode", !link_type 
                        ? Component.translatable("doramachines.waila.redstone_link.transmitter")
                        : Component.translatable("doramachines.waila.redstone_link.receiver"))));
    }
}
