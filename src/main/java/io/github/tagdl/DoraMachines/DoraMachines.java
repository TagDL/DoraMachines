package io.github.tagdl.DoraMachines;

import io.github.lijinhong11.rebarwrench.RebarWrench;
import io.github.pylonmc.pylon.command.PylonCommand;
import io.github.pylonmc.pylon.content.tools.SoulboundRune;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.tagdl.DoraMachines.blocks.LiseletteDisenchanter;
import io.github.tagdl.DoraMachines.blocks.LiseletteEnchanter;
import io.github.tagdl.DoraMachines.blocks.RedstoneClock;
import io.github.tagdl.DoraMachines.blocks.RedstoneLink;
import io.github.tagdl.DoraMachines.command.PortableBackPackCommand;
import io.github.tagdl.DoraMachines.items.AssaultShield;
import io.github.tagdl.DoraMachines.items.Dagger;
import io.github.tagdl.DoraMachines.items.GoldenBoneMeal;
import io.github.tagdl.DoraMachines.items.PayToWin;
import io.github.tagdl.DoraMachines.items.RoadTool;
import io.github.tagdl.DoraMachines.items.Slingshot;
import io.github.tagdl.DoraMachines.items.VoidPicker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

@SuppressWarnings("unused")
public class DoraMachines extends JavaPlugin implements RebarAddon {

    @Getter private static DoraMachines instance;

    @Override
    public void onEnable() {
        instance = this;
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(PortableBackPackCommand.ROOT);
        });
        registerWithRebar();
        PluginManager pm = Bukkit.getPluginManager();
        DoraMachinesItems.initialize();
        DoraMachinesBlocks.initialize();
        DoraMachinesPages.initialise();
        pm.registerEvents(new GoldenBoneMeal.GoldenBoneMealListener(), this);
        pm.registerEvents(new LiseletteEnchanter.PlaceListener(), this);
        pm.registerEvents(new LiseletteDisenchanter.PlaceListener(), this);
        pm.registerEvents(new RoadTool.PlayMoving(), this);
        pm.registerEvents(new VoidPicker.PickupListener(), this);
        pm.registerEvents(new Slingshot.CrossbowLoad(), this);
        pm.registerEvents(new Dagger.PlayerAttack(), this);
        pm.registerEvents(new AssaultShield.AnvilEnchant(), this);
        pm.registerEvents(new PayToWin.PayToWinListener(), this);
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public @NotNull Set<@NotNull Locale> getLanguages() {
        return Set.of(
            Locale.ENGLISH,
            Locale.SIMPLIFIED_CHINESE
        );
    }

    @Override
    public @NotNull Material getMaterial() {
        return Material.NETHERITE_PICKAXE;
    }
    public static void nya() {
        Bukkit.getLogger().info("nya");
    }
}
