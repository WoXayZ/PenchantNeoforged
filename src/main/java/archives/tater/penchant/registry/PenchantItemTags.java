package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class PenchantItemTags {
    private PenchantItemTags() {}

    public static final TagKey<Item> MAX_LEVEL_ENCHANTMENTS = TagKey.create(Registries.ITEM, Penchant.id("max_level_enchantments"));
    public static final TagKey<Item> PROPORTIONAL_ENCHANTMENT_PROGRESS = TagKey.create(Registries.ITEM, Penchant.id("proportional_enchantment_progress"));
    public static final TagKey<Item> LEVEL_UP_BY_KILLING = TagKey.create(Registries.ITEM, Penchant.id("level_up_by_killing"));
    public static final TagKey<Item> DEFAULT_PROGRESS_COST_FACTOR_ONE = TagKey.create(Registries.ITEM, Penchant.id("default_progress_cost_factor_one"));
    public static final TagKey<Item> LEVEL_UP_BY_USE = TagKey.create(Registries.ITEM, Penchant.id("level_up_by_use"));
}
