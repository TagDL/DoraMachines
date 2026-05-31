package io.github.tagdl.DoraMachines.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.github.pylonmc.rebar.item.RebarItem;
import io.github.tagdl.DoraMachines.blocks.PortableBackPack.Item;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class PortableBackPackCommand {
    public final LiteralCommandNode<CommandSourceStack> ROOT = Commands.literal("setbackpackname")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .requires(source -> source.getSender() instanceof Player)
                    .executes(PortableBackPackCommand::setname))
                .build();
    private int setname(CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>you must be a player to run this command");
            return Command.SINGLE_SUCCESS;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        RebarItem item = RebarItem.fromStack(itemStack);
        if (!(item instanceof Item backpack)) {
            sender.sendMessage(Component.translatable("doramachines.message.portable_backpack.not_handle_backpack"));
            return Command.SINGLE_SUCCESS;
        }
        Component name = ctx.getArgument("name", String.class) == null 
                            || ctx.getArgument("name", String.class) == ""
                    ? Component.translatable("doramachines:item:portable_backpack:name")
                    : Component.text(ctx.getArgument("name", String.class));
        backpack.setName(player, name);
        return Command.SINGLE_SUCCESS;
    }    
}
