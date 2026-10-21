package archives.tater.penchant.mixin.loot;

import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Optional;

@Mixin(LootPool.class)
public interface LootPoolAccessor {
    @Accessor("entries")
    List<LootPoolEntryContainer> penchant$getEntries();

    @Accessor("condition")
    Optional<Holder<LootItemCondition>> penchant$getCondition();

    @Accessor("modifier")
    Optional<Holder<LootItemFunction>> penchant$getModifier();

    @Accessor("rolls")
    Holder<ContextIntProvider> penchant$getRolls();

    @Accessor("bonusRolls")
    Holder<ContextFloatProvider> penchant$getBonusRolls();

    @Accessor("name")
    String penchant$getName();

    @Invoker("<init>")
    static LootPool penchant$create(List<LootPoolEntryContainer> entries, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier, Holder<ContextIntProvider> rolls, Holder<ContextFloatProvider> bonusRolls, Optional<String> name) {
        throw new AssertionError();
    }
}
