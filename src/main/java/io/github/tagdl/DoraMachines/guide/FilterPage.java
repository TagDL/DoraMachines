package io.github.tagdl.DoraMachines.guide;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.WeightedSet;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.tagdl.DoraMachines.DoraMachinesKeys;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;


public class FilterPage extends SimpleDynamicGuidePage {
    private static final FilterPage INSTANCE = new FilterPage();
    private static Config settings = Settings.get(DoraMachinesKeys.FILTER_BASE);
    private static WeightedSet<ItemStack> filted = settings.getOrThrow("filter_things", ConfigAdapter.WEIGHTED_SET.from(ConfigAdapter.ITEM_STACK));
    private static float totalWeight = filted.stream().map(WeightedSet.Element::weight).reduce(0f, Float::sum);
    @Getter private static final Item button = new PageButton(
            ItemStackBuilder.of(Material.FISHING_ROD)
                    .name(Component.translatable("doramachines.guide.page.filter_things"))
                    .build(),
            INSTANCE
    );
    public FilterPage() {
        super(DoraMachinesKeys.FILTER_THINGS, FilterPage::getButtons);
    }
    
    private static @NotNull List<Item> getButtons() {

        List<Item> buttons = new ArrayList<>();
        for (WeightedSet.Element<ItemStack> element : filted) {
            float normalizedWeight = element.weight() / totalWeight;
            ItemStack stack = ItemStackBuilder.of(element.element().clone())
                    .lore(Component.translatable("doramachines.guide.filter_things").arguments(
                            RebarArgument.of("weight", 
                                UnitFormat.PERCENT.format(Math.round(normalizedWeight * 100)).decimalPlaces(2))
                    ))
                    .build();
            buttons.add(new ItemButton(stack));
        }
        return buttons;
    }
    
}
