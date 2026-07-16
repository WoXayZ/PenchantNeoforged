package archives.tater.penchant.util;

import archives.tater.penchant.PenchantmentDefinition;
import archives.tater.penchant.api.CanEnchantCallback;
import archives.tater.penchant.registry.PenchantFlag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.ModList;

import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.abs;

public class PenchantmentHelper {
    public static final boolean ITEM_DESCRIPTIONS_INSTALLED = ModList.get().isLoaded("item_descriptions");

    private PenchantmentHelper() {}

    public static List<BlockPos> LENIENT_BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, -2, -3, 3, 2, 3)
            .filter(blockPos -> abs(blockPos.getX()) >= 2 || abs(blockPos.getZ()) >= 2 || blockPos.getY() >= 2 || blockPos.getY() <= -1)
            .map(BlockPos::immutable)
            .toList();

    private static final ThreadLocal<Boolean> NO_LEVEL_NAME_CONTEXT = ThreadLocal.withInitial(() -> false);

    public static boolean isNoLevelNameContext() {
        return NO_LEVEL_NAME_CONTEXT.get();
    }

    public static Component getName(Holder<Enchantment> enchantment) {
        NO_LEVEL_NAME_CONTEXT.set(true);
        try {
            return Enchantment.getFullname(enchantment, 1);
        } finally {
            NO_LEVEL_NAME_CONTEXT.remove();
        }
    }

    public static int getProgressCostFactor(Holder<Enchantment> enchantment, int targetLevel) {
        return PenchantmentDefinition.getDefinition(enchantment).getProgressCostFactor(targetLevel);
    }

    public static int getBookRequirement(Holder<Enchantment> enchantment) {
        return PenchantmentDefinition.getDefinition(enchantment).bookRequirement();
    }

    public static int getXpLevelCost(Holder<Enchantment> enchantment) {
        return PenchantmentDefinition.getDefinition(enchantment).experienceCost();
    }

    public static boolean canEnchantItem(ItemStack stack, Holder<Enchantment> enchantment) {
        var result = CanEnchantCallback.ITEM.invoke(stack, enchantment);
        if (result != TriState.DEFAULT) return result.toBoolean(false);
        return stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK) || stack.supportsEnchantment(enchantment);
    }

    public static ItemEnchantments getEnchantments(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack);
    }

    public static boolean hasEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return getEnchantments(stack).getLevel(enchantment) > 0;
    }

    public static boolean canEnchant(ItemStack stack, Holder<Enchantment> enchantment) {
        if (hasEnchantment(stack, enchantment)) return false;
        var result = CanEnchantCallback.STACK.invoke(stack, enchantment);
        if (result != TriState.DEFAULT) return result.toBoolean(false);
        return canEnchantItem(stack, enchantment) && EnchantmentHelper.isEnchantmentCompatible(getEnchantments(stack).keySet(), enchantment);
    }

    public static ItemStack fixBookType(ItemStack stack) {
        if (getEnchantments(stack).isEmpty()) {
            if (stack.is(Items.ENCHANTED_BOOK))
                return stack.transmuteCopy(Items.BOOK);
        } else {
            if (stack.is(Items.BOOK))
                return stack.transmuteCopy(Items.ENCHANTED_BOOK);
        }
        return stack;
    }

    public static ItemStack updateEnchantments(ItemStack stack, Consumer<ItemEnchantments.Mutable> updater) {
        var type = stack.is(Items.BOOK) ? DataComponents.STORED_ENCHANTMENTS : EnchantmentHelper.getComponentType(stack);
        var enchantments = stack.getOrDefault(type, ItemEnchantments.EMPTY);
        var mutable = new ItemEnchantments.Mutable(enchantments);
        updater.accept(mutable);
        var newEnchantments = mutable.toImmutable();
        stack.set(type, newEnchantments);

        return fixBookType(stack);
    }

    public static ItemStack enchant(ItemStack stack, Holder<Enchantment> enchantment) {
        var effectiveStack = stack.is(Items.BOOK) ? stack.transmuteCopy(Items.ENCHANTED_BOOK) : stack;
        effectiveStack.enchant(enchantment, 1);
        return fixBookType(effectiveStack);
    }

    public static List<BlockPos> getBookshelfOffsets(List<BlockPos> original) {
        return PenchantFlag.LENIENT_BOOKSHELF_PLACEMENT.isEnabled() ? LENIENT_BOOKSHELF_OFFSETS : original;
    }

    public static List<BlockPos> getBookshelfOffsets() {
        return getBookshelfOffsets(EnchantingTableBlock.BOOKSHELF_OFFSETS);
    }

    public static int getBookCount(BlockState state) {
        if (state.hasProperty(ChiseledBookShelfBlock.SLOT_0_OCCUPIED))
            return (int) ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.stream().filter(state::getValue).count();
        if (state.hasProperty(LecternBlock.HAS_BOOK))
            return state.getValue(LecternBlock.HAS_BOOK) ? 1 : 0;
        return 3;
    }
}
