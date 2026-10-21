package archives.tater.penchant;

import archives.tater.penchant.api.EnchantmentCompatibleCallback;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.TriState;

import house.greenhouse.enchiridion.api.enchantment.category.EnchantmentCategory;
import house.greenhouse.enchiridion.registry.EnchiridionEnchantmentCategories;
import house.greenhouse.enchiridion.util.EnchantmentCategoryUtil;

/**
 * Optional-mod probes. 1.21.1 Enchiridion publishes the greenhouse category API.
 */
public final class PenchantCompat {
    public static final boolean ENCHIRIDION_INSTALLED = isLoaded("enchiridion");
    public static final boolean MODEST_MAGIC_INSTALLED = isLoaded("modestmagic");

    /**
     * Mixin plugins run before {@link ModList} exists. The loader's mod list is the one available then.
     */
    public static boolean isLoaded(String modId) {
        var loading = FMLLoader.getLoadingModList();
        if (loading != null)
            return loading.getModFileById(modId) != null;
        var loaded = ModList.get();
        return loaded != null && loaded.isLoaded(modId);
    }

    private PenchantCompat() {}

    public static void init() {
        if (ENCHIRIDION_INSTALLED)
            registerEnchiridion();
    }

    public static Holder.Reference<EnchantmentCategory> getCategoryOrUncategorized(Holder<Enchantment> enchantment, HolderLookup.Provider registries) {
        return EnchantmentCategoryUtil.getOptionalFirstEnchantmentCategory(registries, enchantment)
                .orElseGet(() -> {
                    ResourceKey<net.minecraft.core.Registry<EnchantmentCategory>> registryKey = ResourceKey.createRegistryKey(EnchiridionEnchantmentCategories.UNCATEGORISED.registry());
                    return registries.lookupOrThrow(registryKey).getOrThrow(EnchiridionEnchantmentCategories.UNCATEGORISED);
                });
    }

    private static void registerEnchiridion() {
        EnchantmentCompatibleCallback.EVENT.register((enchantment, existing, stack, registries) -> {
            var firstCategory = getCategoryOrUncategorized(enchantment, registries);
            var secondCategory = getCategoryOrUncategorized(existing, registries);
            if (firstCategory.equals(secondCategory) && !EnchantmentCategoryUtil.categoryAcceptsEnchantments(firstCategory, stack, enchantment))
                return TriState.FALSE;
            return TriState.DEFAULT;
        });
    }
}
