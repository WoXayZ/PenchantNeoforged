package archives.tater.penchant.mixin.loot;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Optional;

@Mixin(LootTable.class)
public interface LootTableAccessor {
    @Accessor("paramSet")
    ContextKeySet penchant$getParamSet();

    @Accessor("randomSequence")
    Optional<Identifier> penchant$getRandomSequence();

    @Accessor("pools")
    List<LootPool> penchant$getPools();

    @Accessor("functions")
    List<LootItemFunction> penchant$getFunctions();

    @Invoker("<init>")
    static LootTable penchant$create(ContextKeySet paramSet, Optional<Identifier> randomSequence, List<LootPool> pools, List<LootItemFunction> functions) {
        throw new AssertionError();
    }
}
