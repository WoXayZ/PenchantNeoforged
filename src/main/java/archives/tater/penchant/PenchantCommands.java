package archives.tater.penchant;

import archives.tater.penchant.component.EnchantmentProgress;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.LEVEL_GAMEMASTERS;

public class PenchantCommands {
    private static final DynamicCommandExceptionType BAD_SLOT = new DynamicCommandExceptionType(
            slot -> Component.translatable("commands.item.target.no_such_slot", slot)
    );

    public static void register(RegisterCommandsEvent event) {
        var root = literal("penchant")
                .requires(source -> source.hasPermission(LEVEL_GAMEMASTERS))
                .then(literal("add_progress")
                        .then(literal("block")
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .then(amountSlot(PenchantCommands::addBlockProgress))))
                        .then(literal("entity")
                                .then(argument("targets", EntityArgument.entities())
                                        .then(amountSlot(PenchantCommands::addEntityProgress)))));

        event.getDispatcher().register(root);
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Integer> amountSlot(ProgressAction action) {
        return argument("slot", SlotArgument.slot())
                .executes(context -> action.run(context, 1))
                .then(argument("increase", integer())
                        .executes(context -> action.run(context, getInteger(context, "increase"))));
    }

    private static int addBlockProgress(CommandContext<CommandSourceStack> context, int increase) throws CommandSyntaxException {
        var source = context.getSource();
        var pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        var slot = SlotArgument.getSlot(context, "slot");
        if (!(source.getLevel().getBlockEntity(pos) instanceof Container container) || slot < 0 || slot >= container.getContainerSize()) {
            throw BAD_SLOT.create(slot);
        }
        var stack = container.getItem(slot);
        EnchantmentProgress.addToProgress(stack, increase, null);
        container.setItem(slot, stack);
        source.sendSuccess(() -> Component.translatable("commands.penchant.add_progress.success", increase), true);
        return 1;
    }

    private static int addEntityProgress(CommandContext<CommandSourceStack> context, int increase) throws CommandSyntaxException {
        var source = context.getSource();
        var slot = SlotArgument.getSlot(context, "slot");
        var changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            var access = entity.getSlot(slot);
            if (access == null) continue;
            var stack = access.get();
            EnchantmentProgress.addToProgress(stack, increase, entity instanceof LivingEntity living ? living : null);
            if (access.set(stack)) changed++;
        }
        if (changed == 0) throw BAD_SLOT.create(slot);
        source.sendSuccess(() -> Component.translatable("commands.penchant.add_progress.success", increase), true);
        return changed;
    }

    @FunctionalInterface
    private interface ProgressAction {
        int run(CommandContext<CommandSourceStack> context, int increase) throws CommandSyntaxException;
    }
}
