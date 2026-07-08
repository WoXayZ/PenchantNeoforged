package archives.tater.penchant.mixin.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Optional;

@Mixin(LootPool.class)
public interface LootPoolAccessor {
    @Accessor("entries")
    List<LootPoolEntryContainer> penchant$getEntries();

    @Accessor("conditions")
    List<LootItemCondition> penchant$getConditions();

    @Accessor("functions")
    List<LootItemFunction> penchant$getFunctions();

    @Accessor("rolls")
    NumberProvider penchant$getRolls();

    @Accessor("bonusRolls")
    NumberProvider penchant$getBonusRolls();

    @Accessor("name")
    String penchant$getName();

    @Invoker("<init>")
    static LootPool penchant$create(List<LootPoolEntryContainer> entries, List<LootItemCondition> conditions, List<LootItemFunction> functions, NumberProvider rolls, NumberProvider bonusRolls, Optional<String> name) {
        throw new AssertionError();
    }
}
