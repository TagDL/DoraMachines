package io.github.tagdl.DoraMachines;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.tagdl.DoraMachines.blocks.FilterBase;
import io.github.tagdl.DoraMachines.blocks.IndustrialMiner;
import io.github.tagdl.DoraMachines.blocks.LiseletteDisenchanter;
import io.github.tagdl.DoraMachines.blocks.LiseletteEnchanter;
import io.github.tagdl.DoraMachines.blocks.MultiFluidTank;
import io.github.tagdl.DoraMachines.blocks.TimingCharger;

import org.bukkit.Material;


public final class DoraMachinesBlocks {

    public static void initialize() {
        RebarBlock.register(DoraMachinesKeys.INDUSTRIAL_MINER, Material.BLAST_FURNACE, IndustrialMiner.class);
        RebarBlock.register(DoraMachinesKeys.FILTER_BASE, Material.PRISMARINE_WALL, FilterBase.class);
        RebarBlock.register(DoraMachinesKeys.MULTI_FLUID_TANK, Material.LIGHT_BLUE_STAINED_GLASS, MultiFluidTank.class);
        RebarBlock.register(DoraMachinesKeys.TIMING_CHARGER, Material.RED_GLAZED_TERRACOTTA, TimingCharger.class);
        RebarBlock.register(DoraMachinesKeys.LISELETTE_ENCHANTER, Material.POLISHED_DIORITE_SLAB, LiseletteEnchanter.class);
        RebarBlock.register(DoraMachinesKeys.LISELETTE_DISENCHANTER, Material.POLISHED_ANDESITE_SLAB, LiseletteDisenchanter.class);
    }
}
