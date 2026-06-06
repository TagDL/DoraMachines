package io.github.tagdl.DoraMachines.blocks;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import io.github.lijinhong11.rebarwrench.api.Wrenchable;
import io.github.lijinhong11.rebarwrench.api.properties.Property;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.tagdl.DoraMachines.DoraMachines;
import net.kyori.adventure.text.format.NamedTextColor;

public class TempBlock extends RebarBlock implements InteractRebarBlockHandler{
    private final NamespacedKey INT = new NamespacedKey(DoraMachines.getInstance(), "ivalue");

    public static final Wrenchable WRENCHABLE = Wrenchable.builder()
            .addProperty("color", Property.builder(NamedTextColor.class)
                            .defaultIndex(0)
                            .possibleValues(NamedTextColor.BLUE, NamedTextColor.GREEN)
                            .onValueChange((m, oldColor, newColor) -> {
                                Block b = m.getBlock();
                                b.setType(newColor == NamedTextColor.BLUE ? Material.BLUE_WOOL : Material.GREEN_WOOL);
                            })
                            .build()
            )
            .addProperty("ivalue", Property.builder(int.class)
                    .defaultIndex(0)
                    .possibleValues(0, 1, 2, 3, 4, 5)
                    .onValueChange((m, oldInt, newInt) -> {
                        ((TempBlock) m).value = newInt;
                    })
                    .build())
            .build();

    private int value = 0;

    public TempBlock(@NonNull Block block, @NonNull BlockCreateContext context) {
        super(block, context);
    }

    public TempBlock(@NonNull Block block, @NonNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onInteractedWith(@NonNull PlayerInteractEvent e, @NonNull EventPriority priority) {
        if (!e.getAction().isRightClick()) {
            return;
        }

        e.getPlayer().sendMessage("The int value is " + value);
    }

    @Override
    public void write(@NonNull PersistentDataContainer pdc) {
        pdc.set(INT, PersistentDataType.INTEGER, value);
    }
}
