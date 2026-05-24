package io.github.tagdl.DoraMachines;

import io.github.pylonmc.pylon.content.tools.SoulboundRune;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.tagdl.DoraMachines.blocks.LiseletteDisenchanter;
import io.github.tagdl.DoraMachines.blocks.LiseletteEnchanter;
import io.github.tagdl.DoraMachines.items.GoldenBoneMeal;
import io.github.tagdl.DoraMachines.items.RoadTool;
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

    // Stores the instance of the addon (there's only ever one)
    @Getter private static DoraMachines instance;

    // Called when the addon is enabled
    @Override
    public void onEnable() {
        instance = this;

        // Every Rebar addon must call this BEFORE doing anything Rebar-related
        registerWithRebar();
        PluginManager pm = Bukkit.getPluginManager();
        DoraMachinesItems.initialize();
        DoraMachinesBlocks.initialize();
        DoraMachinesPages.initialise();
        pm.registerEvents(new GoldenBoneMeal.GoldenBoneMealListener(), this);
        pm.registerEvents(new LiseletteEnchanter.PlaceListener(), this);
        pm.registerEvents(new LiseletteDisenchanter.PlaceListener(), this);
        pm.registerEvents(new RoadTool.PlayMoving(), this);
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
        return Material.DEAD_BUSH;
    }
}
