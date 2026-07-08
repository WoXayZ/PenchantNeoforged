package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class PenchantEnchantmentTags {
    private PenchantEnchantmentTags() {}

    private static TagKey<Enchantment> of(String path) {
        return TagKey.create(Registries.ENCHANTMENT, Penchant.id(path));
    }

    public static final TagKey<Enchantment> DISABLED = of("disabled");
    public static final TagKey<Enchantment> NO_LEVELING = of("no_leveling");
}
