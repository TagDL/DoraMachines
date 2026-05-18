package io.github.username.exampleaddon;

import io.github.pylonmc.rebar.addon.RebarAddon;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

@SuppressWarnings("unused")
public class ExampleAddon extends JavaPlugin implements RebarAddon {

    // Stores the instance of the addon (there's only ever one)
    @Getter private static ExampleAddon instance;

    // Called when the addon is enabled
    @Override
    public void onEnable() {
        instance = this;

        // Every Rebar addon must call this BEFORE doing anything Rebar-related
        registerWithRebar();

        ExampleAddonItems.initialize();
        ExampleAddonBlocks.initialize();
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public @NotNull Set<@NotNull Locale> getLanguages() {
        return Set.of(Locale.ENGLISH);
    }

    @Override
    public @NotNull Material getMaterial() {
        return Material.DEAD_BUSH;
    }
}
