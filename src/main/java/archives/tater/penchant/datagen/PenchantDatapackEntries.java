package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.enchantment.UnbreakableEffect;
import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.registry.PenchantEnchantments;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.EnchantmentsByCostWithDifficulty;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.world.level.storage.loot.LootPool.lootPool;
import static net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem;
import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;
import static net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly;

/**
 * Bootstrap methods for Penchant's dynamic (datapack) registry entries. Each method mirrors one of the Fabric
 * {@code FabricDynamicRegistryProvider} subclasses, adapted to NeoForge's {@link BootstrapContext} datagen API.
 */
public final class PenchantDatapackEntries {
    private PenchantDatapackEntries() {}

    // region durability_rework enchantments

    public static void bootstrapDurabilityEnchantments(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        context.register(Enchantments.UNBREAKING, new Enchantment.Builder(new Enchantment.EnchantmentDefinition(
                items.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                Optional.empty(),
                5,
                5,
                new Enchantment.Cost(5, 20),
                new Enchantment.Cost(35, 20),
                1,
                List.of(EquipmentSlotGroup.ANY)
        ))
                .withEffect(EnchantmentEffectComponents.ITEM_DAMAGE,
                        new RemoveBinomial(new LevelBasedValue.Fraction(
                                LevelBasedValue.perLevel(0, 1),
                                LevelBasedValue.constant(4)
                        ))
                )
                .withSpecialEffect(PenchantEnchantments.UNBREAKABLE, List.of(
                        new UnbreakableEffect(MinMaxBounds.Ints.atLeast(5))
                ))
                .build(Enchantments.UNBREAKING.location())
        );

        context.register(Enchantments.MENDING, new Enchantment.Builder(new Enchantment.EnchantmentDefinition(
                HolderSet.empty(),
                Optional.empty(),
                1,
                1,
                new Enchantment.Cost(99, 0),
                new Enchantment.Cost(99, 0),
                1,
                List.of()
        )).build(Enchantments.MENDING.location()));
    }

    // endregion

    // region loot_rework enchantment providers

    public static void bootstrapLootEnchantmentProviders(BootstrapContext<EnchantmentProvider> context) {
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        context.register(VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, new EnchantmentsByCostWithDifficulty(
                enchantments.getOrThrow(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT),
                5,
                25
        ));
    }

    // endregion

    // region loot_rework loot modifications

    public static void bootstrapLootModifications(BootstrapContext<LootModification> context) {
        HolderGetter<Enchantment> registry = context.lookup(Registries.ENCHANTMENT);

        addInject(context, BuiltInLootTables.IGLOO_CHEST, createBooks(registry, 4, Enchantments.FROST_WALKER));
        addInject(context, BuiltInLootTables.NETHER_BRIDGE, createBooks(registry, 6, Enchantments.FIRE_ASPECT, Enchantments.FLAME));
        addInject(context, BuiltInLootTables.RUINED_PORTAL, createBooks(registry, 14, Enchantments.FIRE_ASPECT, Enchantments.FLAME));
        addInject(context, BuiltInLootTables.ABANDONED_MINESHAFT, createBooks(registry, 8, Enchantments.SILK_TOUCH, Enchantments.FORTUNE));
        addInject(context, BuiltInLootTables.SIMPLE_DUNGEON, createBooks(registry, 14, Enchantments.SILK_TOUCH, Enchantments.FORTUNE));
        addInject(context, BuiltInLootTables.SHIPWRECK_TREASURE, createBooks(registry, 18, Enchantments.RESPIRATION, Enchantments.DEPTH_STRIDER));
        context.register(ResourceKey.create(LootModification.KEY, Penchant.id("minecraft/chests/underwater_ruin")), new LootModification(
                List.of(BuiltInLootTables.UNDERWATER_RUIN_SMALL, BuiltInLootTables.UNDERWATER_RUIN_BIG),
                Stream.of(
                        createBooks(registry, 8, Enchantments.RESPIRATION, Enchantments.DEPTH_STRIDER),
                        createBooks(registry, 18, Enchantments.CHANNELING, Enchantments.RIPTIDE)
                ).map(LootPool.Builder::build).toList(),
                List.of(),
                Optional.empty()
        ));
        addInject(context, BuiltInLootTables.BURIED_TREASURE,
                createBooks(registry, Enchantments.CHANNELING, Enchantments.RIPTIDE),
                createBooks(registry, 8, Enchantments.RESPIRATION, Enchantments.DEPTH_STRIDER)
        );
        addInject(context, BuiltInLootTables.JUNGLE_TEMPLE, createBooks(registry, 4, Enchantments.INFINITY));
        addInject(context, BuiltInLootTables.DESERT_PYRAMID, createBooks(registry, 9, Enchantments.THORNS));
        addInject(context, BuiltInLootTables.PILLAGER_OUTPOST, createBooks(registry, 4, Enchantments.MULTISHOT));
    }

    private static void addInject(BootstrapContext<LootModification> context, ResourceKey<LootTable> target, LootPool.Builder... inject) {
        context.register(
                ResourceKey.create(LootModification.KEY, Penchant.id(target.location().getNamespace() + "/" + target.location().getPath())),
                new LootModification(
                        List.of(target),
                        Arrays.stream(inject).map(LootPool.Builder::build).toList(),
                        List.of(),
                        Optional.empty()
                )
        );
    }

    @SafeVarargs
    private static LootPool.Builder createBooks(HolderGetter<Enchantment> registry, ResourceKey<Enchantment>... enchantments) {
        return createBooks(registry, 1, 0, enchantments);
    }

    @SafeVarargs
    private static LootPool.Builder createBooks(HolderGetter<Enchantment> registry, int emptyWeight, ResourceKey<Enchantment>... enchantments) {
        return createBooks(registry, 1, emptyWeight, enchantments);
    }

    @SafeVarargs
    private static LootPool.Builder createBooks(HolderGetter<Enchantment> registry, int singleBookWeight, int emptyWeight, ResourceKey<Enchantment>... enchantments) {
        var pool = lootPool();

        if (emptyWeight > 0)
            pool.add(emptyItem()
                    .setWeight(emptyWeight)
            );

        for (var enchantment : enchantments) {
            pool.add(lootTableItem(Items.BOOK)
                    .setWeight(singleBookWeight)
                    .apply(new SetEnchantmentsFunction.Builder()
                            .withEnchantment(registry.getOrThrow(enchantment), exactly(1))
                    )
            );
        }

        return pool;
    }

    // endregion
}
