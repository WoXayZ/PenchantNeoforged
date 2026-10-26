package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class PenchantEntityTags {
    private PenchantEntityTags() {}

    public static final TagKey<EntityType<?>> KILL_LEVELS_UP_EQUIPMENT = TagKey.create(Registries.ENTITY_TYPE, Penchant.id("kill_levels_up_equipment"));
}
