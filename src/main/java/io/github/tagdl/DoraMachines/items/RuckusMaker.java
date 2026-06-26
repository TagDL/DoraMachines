package io.github.tagdl.DoraMachines.items;

import static java.lang.Math.random;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;

public class RuckusMaker extends RebarItem implements InteractRebarItemHandler {
    public RuckusMaker(@NotNull ItemStack stack) {
        super(stack);
    }
    private static final ConfigSection setting = ConfigSection.fromSettings(DoraMachinesKeys.RUCKUS_MAKER);
    private static final float VOLUME = setting.getOrThrow("volume", ConfigAdapter.FLOAT);
    private static final float PITCH = setting.getOrThrow("pitch", ConfigAdapter.FLOAT);
    private static final int PARTICLE_AMOUNT = setting.getOrThrow("particle-amount", ConfigAdapter.INTEGER);
    private static final List<NamespacedKey> SOUND_LIST = Registry.SOUNDS.keyStream().toList();
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) return;
        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }
        Player player = event.getPlayer();
        startSound(player);
        for (int i = 0; i < PARTICLE_AMOUNT; i++) 
            playParticle(player.getLocation().add(
                ThreadLocalRandom.current().nextDouble(-2, 2),
                ThreadLocalRandom.current().nextDouble(0.5),
                ThreadLocalRandom.current().nextDouble(-2, 2)));
        
    }
    private static void playParticle(Location location) {
        int indexParticle = (int) (random() * Particle.values().length);
        Particle particle = Particle.values()[indexParticle];
        if (particle.getDataType().equals(Void.class)) location.getWorld().spawnParticle(particle, location, 1);
        else if (particle == Particle.DUST) {
            location.getWorld().spawnParticle(particle, location, 1, 
                new Particle.DustOptions(getRandomColor(), 2));
        } else if (particle == Particle.DUST_COLOR_TRANSITION) {
            location.getWorld().spawnParticle(particle, location, 1, 
                new Particle.DustTransition(getRandomColor(), getRandomColor(), 2));
        } else playParticle(location);
    }
    private static void startSound(Player player) {
        int indexSound = (int) (random() * SOUND_LIST.size());
        Sound sound = Registry.SOUNDS.get(SOUND_LIST.get(indexSound));
        if (Registry.SOUNDS.getKey(sound).getKey().startsWith("music_disc.")) startSound(player);
        else player.getWorld().playSound(player.getLocation(), sound, VOLUME, PITCH);
    }
    private static Color getRandomColor() {
        return Color.fromRGB(
                    ThreadLocalRandom.current().nextInt(256),
                    ThreadLocalRandom.current().nextInt(256),
                    ThreadLocalRandom.current().nextInt(256));
    }

}
