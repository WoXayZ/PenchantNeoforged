package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEntityTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class EntityTagGenerator extends PenchantTagsProvider<EntityType<?>> {
    public EntityTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(PenchantEntityTags.KILL_LEVELS_UP_EQUIPMENT);
    }
}
