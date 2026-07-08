package archives.tater.penchant.loot;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.mixin.loot.LootPoolAccessor;
import archives.tater.penchant.mixin.loot.LootTableAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record LootModification(
        List<ResourceKey<LootTable>> targets,
        List<LootPool> pools,
        List<LootItemFunction> functions,
        Optional<LootPoolPatch> modifyPools
) {
    public static final Codec<LootModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.LOOT_TABLE).listOf(1, Integer.MAX_VALUE).fieldOf("targets").forGetter(LootModification::targets),
            LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(LootModification::pools),
            LootItemFunctions.TYPED_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(LootModification::functions),
            LootPoolPatch.CODEC.optionalFieldOf("modify_pools").forGetter(LootModification::modifyPools)
    ).apply(instance, LootModification::new));

    public static final ResourceKey<Registry<LootModification>> KEY = ResourceKey.createRegistryKey(Penchant.id("loot_modification"));

    public static void init() {
        NeoForge.EVENT_BUS.addListener(LootModification::onLootTableLoad);
    }

    private static void onLootTableLoad(LootTableLoadEvent event) {
        var lookupOpt = event.getRegistries().lookup(KEY);
        if (lookupOpt.isEmpty()) return;

        var key = event.getKey();
        var applicable = lookupOpt.get().listElements()
                .map(Holder::value)
                .filter(modification -> modification.matches(key))
                .toList();
        if (applicable.isEmpty()) return;

        LootTable table = event.getTable();
        var accessor = (LootTableAccessor) table;

        List<LootPool> pools = new ArrayList<>(accessor.penchant$getPools());
        List<LootItemFunction> functions = new ArrayList<>(accessor.penchant$getFunctions());

        for (var modification : applicable) {
            modification.apply(pools, functions);
        }

        event.setTable(LootTableAccessor.penchant$create(
                accessor.penchant$getParamSet(),
                accessor.penchant$getRandomSequence(),
                pools,
                functions
        ));
    }

    public boolean matches(ResourceKey<LootTable> key) {
        return targets.contains(key);
    }

    private void apply(List<LootPool> pools, List<LootItemFunction> functions) {
        pools.addAll(this.pools);
        functions.addAll(this.functions);
        modifyPools.ifPresent(patch -> pools.replaceAll(patch::apply));
    }

    public record LootPoolPatch(
            List<LootPoolEntryContainer> entries,
            List<LootItemCondition> conditions,
            List<LootItemFunction> functions
    ) {
        public static final Codec<LootPoolPatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(LootPoolPatch::entries),
                LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(LootPoolPatch::conditions),
                LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(LootPoolPatch::functions)
        ).apply(instance, LootPoolPatch::new));

        public LootPool apply(LootPool pool) {
            var accessor = (LootPoolAccessor) pool;

            List<LootPoolEntryContainer> newEntries = new ArrayList<>(accessor.penchant$getEntries());
            newEntries.addAll(entries);

            List<LootItemCondition> newConditions = new ArrayList<>(accessor.penchant$getConditions());
            newConditions.addAll(conditions);

            List<LootItemFunction> newFunctions = new ArrayList<>(accessor.penchant$getFunctions());
            newFunctions.addAll(functions);

            return LootPoolAccessor.penchant$create(
                    newEntries,
                    newConditions,
                    newFunctions,
                    accessor.penchant$getRolls(),
                    accessor.penchant$getBonusRolls(),
                    Optional.ofNullable(accessor.penchant$getName())
            );
        }
    }
}
