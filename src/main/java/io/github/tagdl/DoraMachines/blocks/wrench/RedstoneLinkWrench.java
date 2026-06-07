package io.github.tagdl.DoraMachines.blocks.wrench;

import org.bukkit.entity.Player;

import io.github.lijinhong11.rebarwrench.api.WrenchAction;
import io.github.lijinhong11.rebarwrench.api.WrenchResult;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.tagdl.DoraMachines.blocks.RedstoneLink;
import net.kyori.adventure.text.Component;

public class RedstoneLinkWrench {
    public static WrenchResult onWrench(Player player, RebarBlock block, WrenchAction action) {
        RedstoneLink redstoneLink = (RedstoneLink) block;
        if (!action.equals(WrenchAction.CONFIGURE)) return WrenchResult.SUCCESS;
        redstoneLink.setLinkType(!redstoneLink.getLinkType());;
        player.sendActionBar(!redstoneLink.getLinkType()
            ? Component.translatable("doramachines.message.redstone_link.transmitter")
            : Component.translatable("doramachines.message.redstone_link.receiver")
        );
        return WrenchResult.SUCCESS;
    }
}
