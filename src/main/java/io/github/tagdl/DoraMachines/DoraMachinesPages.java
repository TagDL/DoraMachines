package io.github.tagdl.DoraMachines;

import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;

import org.bukkit.NamespacedKey;
public class DoraMachinesPages {
    public static final SimpleStaticGuidePage Dora_Machines = new SimpleStaticGuidePage(new NamespacedKey(DoraMachines.getInstance(), "doramachines"));
    public static final SimpleStaticGuidePage TOOL = new SimpleStaticGuidePage(new NamespacedKey(DoraMachines.getInstance(), "doramachines_tool"));
    public static final SimpleStaticGuidePage BLOCK = new SimpleStaticGuidePage(new NamespacedKey(DoraMachines.getInstance(), "doramachines_block"));
    public static final SimpleStaticGuidePage TOY = new SimpleStaticGuidePage(new NamespacedKey(DoraMachines.getInstance(), "doramachines_toy"));
    public static void initialise(){
        RebarGuide.getRootPage().addPage(DoraMachinesItems.EXPLOSION_PICKAXE, Dora_Machines);
        Dora_Machines.addPage(DoraMachinesItems.EXPLOSION_SHOVEL, TOOL);
        Dora_Machines.addPage(DoraMachinesItems.FILTER_BASE, BLOCK);
        Dora_Machines.addPage(DoraMachinesItems.SLINGSHOT, TOY);
    }
}
