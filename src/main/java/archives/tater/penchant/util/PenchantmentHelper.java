package archives.tater.penchant.util;

import archives.tater.penchant.PenchantmentDefinition;
import archives.tater.penchant.api.CanEnchantCallback;
import archives.tater.penchant.compat.MaxProtectionCompat;
import archives.tater.penchant.mixin.leveling.EnchantmentDefinitionAccessor;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.LecternBlock;

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

    /**
     * The level Penchant levels an enchantment up to.
     *
     * <p>{@link Enchantment#getMaxLevel()} is used when it disagrees with
     * {@link Enchantment.EnchantmentDefinition#maxLevel()} — that is how Remove Enchantment Limits
     * (and similar) raise <em>or</em> lower a cap without touching the definition accessor.
     * Improved Multishot instead injects {@code maxLevel()} itself, so both values collapse to 3
     * for every crossbow-capable enchantment; in that case the datapack field wins, and Unbreaking
     * stays at 5. A mixin that genuinely raises the accessor (Multishot 1 → 3) is still honoured
     * via {@code Math.max} with the stored field.
     */
    public static int getMaxLevel(Holder<Enchantment> enchantment) {
        return getMaxLevel(enchantment.value());
    }

    public static int getMaxLevel(Enchantment enchantment) {
        var definition = enchantment.definition();
        int stored = ((EnchantmentDefinitionAccessor) (Object) definition).getPenchantMaxLevel();
        int fromDefinition = definition.maxLevel();
        int reported = enchantment.getMaxLevel();
        if (reported != fromDefinition) {
            return reported;
        }
        return Math.max(stored, fromDefinition);
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

    /**
     * Whether the item could ever take this enchantment, ignoring what the stack currently carries.
     *
     * <p>Asked against a pristine stack: mods veto {@code supportsEnchantment} based on the
     * enchantments already present, which would otherwise make entries vanish from the table menu
     * as soon as the player picks something in the same category.
     */
    public static boolean canEnchantItem(ItemStack stack, Holder<Enchantment> enchantment) {
        var result = CanEnchantCallback.ITEM.invoke(stack, enchantment);
        if (result != TriState.DEFAULT) return result.toBoolean(false);
        return stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK)
                || stack.getItem().getDefaultInstance().supportsEnchantment(enchantment);
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
        return canEnchantItem(stack, enchantment)
                && stack.supportsEnchantment(enchantment)
                && isCompatibleWithExisting(stack, enchantment);
    }


    public static boolean areCompatible(Holder<Enchantment> first, Holder<Enchantment> second) {
        if (MaxProtectionCompat.allowsTogether(first, second)) return true;
        return Enchantment.areCompatible(first, second);
    }

    public static boolean isCompatibleWithExisting(ItemStack stack, Holder<Enchantment> enchantment) {
        for (var other : getEnchantments(stack).keySet()) {
            if (!areCompatible(enchantment, other)) return false;
        }
        return true;
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

    /**
     * How many books' worth of enchanting power a block is worth to the reworked table.
     *
     * <p>Blocks other than bookshelves are measured through NeoForge's enchant power API, so a modded
     * block advertising a fractional power counts as that fraction of a full three-book shelf.
     */
    public static float getBookCount(LevelReader level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.hasProperty(ChiseledBookShelfBlock.SLOT_0_OCCUPIED))
            return (int) ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.stream().filter(state::getValue).count();
        if (state.hasProperty(LecternBlock.HAS_BOOK))
            return state.getValue(LecternBlock.HAS_BOOK) ? 1 : 0;
        return 3 * state.getEnchantPowerBonus(level, pos);
    }
}
