package archives.tater.penchant.mixin.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Optional;

@Mixin(LootTable.class)
public interface LootTableAccessor {
    @Accessor("paramSet")
    LootContextParamSet penchant$getParamSet();

    @Accessor("randomSequence")
    Optional<ResourceLocation> penchant$getRandomSequence();

    @Accessor("pools")
    List<LootPool> penchant$getPools();

    @Accessor("functions")
    List<LootItemFunction> penchant$getFunctions();

    @Invoker("<init>")
    static LootTable penchant$create(LootContextParamSet paramSet, Optional<ResourceLocation> randomSequence, List<LootPool> pools, List<LootItemFunction> functions) {
        throw new AssertionError();
    }
}
