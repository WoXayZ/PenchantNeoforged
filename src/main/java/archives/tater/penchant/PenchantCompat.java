package archives.tater.penchant;

import archives.tater.penchant.api.EnchantmentCompatibleCallback;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import org.vulpixel.enchiridion.world.item.enchantment.category.EnchantmentCategory;
import org.vulpixel.enchiridion.world.item.enchantment.category.EnchantmentCategoryHelper;
import org.vulpixel.enchiridion.world.item.enchantment.category.EnchiridionEnchantmentCategories;

public final class PenchantCompat {
    public static final boolean ENCHIRIDION_INSTALLED = isLoaded("enchiridion");
    public static final boolean MODEST_MAGIC_INSTALLED = isLoaded("modestmagic");

    /**
     * Mixin plugins run before {@link ModList} exists. The loader's mod list is the one available then.
     */
    public static boolean isLoaded(String modId) {
        var loader = FMLLoader.getCurrentOrNull();
        if (loader != null && loader.getLoadingModList() != null)
            return loader.getLoadingModList().getModFileById(modId) != null;
        var loaded = ModList.get();
        return loaded != null && loaded.isLoaded(modId);
    }

    private PenchantCompat() {}

    public static void init() {
        if (ENCHIRIDION_INSTALLED)
            registerEnchiridion();
    }

    public static Holder.Reference<EnchantmentCategory> getCategoryOrUncategorized(Holder<Enchantment> enchantment, HolderLookup.Provider registries) {
        return EnchantmentCategoryHelper.getFirstEnchantmentCategoryForEnchantment(registries, enchantment)
                .orElseGet(() -> registries.getOrThrow(EnchiridionEnchantmentCategories.UNCATEGORIZED));
    }

    private static void registerEnchiridion() {
        EnchantmentCompatibleCallback.EVENT.register((enchantment, existing, stack, registries) -> {
            var firstCategory = getCategoryOrUncategorized(enchantment, registries);
            var secondCategory = getCategoryOrUncategorized(existing, registries);
            if (firstCategory.equals(secondCategory) && EnchantmentCategoryHelper.isCategoryLimitReached(firstCategory, stack, enchantment))
                return TriState.FALSE;
            return TriState.DEFAULT;
        });
    }
}
