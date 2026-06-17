package io.github.tagdl.DoraMachines.blocks.wrench;

import org.bukkit.entity.Player;

import io.github.lijinhong11.rebarwrench.api.WrenchAction;
import io.github.lijinhong11.rebarwrench.api.WrenchResult;
import io.github.pylonmc.rebar.block.RebarBlock;

public class NormalWrench {
    public static WrenchResult onWrench(Player player, RebarBlock block, WrenchAction action) {
        return WrenchResult.SUCCESS;
    }
}
