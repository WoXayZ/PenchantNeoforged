package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PenchantItemTags {

    private static TagKey<Item> of(String path) {
        return TagKey.create(Registries.ITEM, Penchant.id(path));
    }

    public static final TagKey<Item> MAX_LEVEL_ENCHANTMENTS = of("max_level_enchantments");
    public static final TagKey<Item> PROPORTIONAL_ENCHANTMENT_PROGRESS = of("proportional_enchantment_progress");
    public static final TagKey<Item> LEVEL_UP_BY_KILLING = of("level_up_by_killing");
    public static final TagKey<Item> DEFAULT_PROGRESS_COST_FACTOR_ONE = of("default_progress_cost_factor_one");
    public static final TagKey<Item> LEVEL_UP_BY_USE = of("level_up_by_use");
}
