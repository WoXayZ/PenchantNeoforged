package archives.tater.penchant;

import archives.tater.penchant.datagen.BlockTagGenerator;
import archives.tater.penchant.datagen.BookshelfBlockTagGenerator;
import archives.tater.penchant.datagen.CurseEnchantmentTagGenerator;
import archives.tater.penchant.datagen.DurabilityEnchantmentTagGenerator;
import archives.tater.penchant.datagen.EnchantmentTagGenerator;
import archives.tater.penchant.datagen.FlagTagGenerator;
import archives.tater.penchant.datagen.ItemTagGenerator;
import archives.tater.penchant.datagen.LootAdvancementGenerator;
import archives.tater.penchant.datagen.LootEnchantmentTagGenerator;
import archives.tater.penchant.datagen.PackMetaGen;
import archives.tater.penchant.datagen.PenchantDatapackEntries;
import archives.tater.penchant.datagen.TableAdvancementGenerator;
import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.registry.PenchantModules;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Drives NeoForge data generation. The main mod data pack is emitted to the vanilla pack output while every
 * toggleable module ships as a built-in datapack under {@code resourcepacks/<id>} (matching {@link PenchantModules}).
 */
public final class PenchantDataGenerator {
    // Penchant's durability/loot modules override vanilla (minecraft-namespaced) enchantments and enchantment
    // providers, so those namespaces must be whitelisted for the datapack registry generator to emit them.
    private static final Set<String> VANILLA_OVERRIDE_IDS = Set.of(Penchant.MOD_ID, "minecraft");

    private PenchantDataGenerator() {}

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Main mod data pack.
        var main = generator.getVanillaPack(true);
        main.addProvider(output -> new FlagTagGenerator(output, lookup, existingFileHelper));
        main.addProvider(output -> new EnchantmentTagGenerator(output, lookup, existingFileHelper));
        main.addProvider(output -> new BlockTagGenerator(output, lookup, existingFileHelper));
        main.addProvider(output -> new ItemTagGenerator(output, lookup, existingFileHelper));

        // durability_rework
        var durability = builtinPack(generator, PenchantModules.DURABILITY_REWORK);
        durability.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup,
                new RegistrySetBuilder().add(Registries.ENCHANTMENT, PenchantDatapackEntries::bootstrapDurabilityEnchantments),
                VANILLA_OVERRIDE_IDS));
        durability.addProvider(output -> new DurabilityEnchantmentTagGenerator(output, lookup, existingFileHelper));

        // bookshelf_placement
        var bookshelf = builtinPack(generator, PenchantModules.BOOKSHELF_PLACEMENT);
        bookshelf.addProvider(output -> new FlagTagGenerator(output, lookup, existingFileHelper, PenchantFlag.LENIENT_BOOKSHELF_PLACEMENT));
        bookshelf.addProvider(output -> new BookshelfBlockTagGenerator(output, lookup, existingFileHelper));

        // no_anvil_books
        var anvil = builtinPack(generator, PenchantModules.NO_ANVIL_BOOKS);
        anvil.addProvider(output -> new FlagTagGenerator(output, lookup, existingFileHelper, PenchantFlag.NO_ANVIL_BOOKS));

        // table_rework
        var table = builtinPack(generator, PenchantModules.TABLE_REWORK);
        table.addProvider(output -> new FlagTagGenerator(output, lookup, existingFileHelper, PenchantFlag.REWORKED_TABLE_MENU));
        table.addProvider(output -> new AdvancementProvider(output, lookup, List.of(new TableAdvancementGenerator())));

        // loot_rework
        var loot = builtinPack(generator, PenchantModules.LOOT_REWORK);
        loot.addProvider(output -> new FlagTagGenerator(output, lookup, existingFileHelper, PenchantFlag.ZOMBIE_SPAWN_PICKAXE));
        loot.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup,
                new RegistrySetBuilder()
                        .add(LootModification.KEY, PenchantDatapackEntries::bootstrapLootModifications)
                        .add(Registries.ENCHANTMENT_PROVIDER, PenchantDatapackEntries::bootstrapLootEnchantmentProviders),
                VANILLA_OVERRIDE_IDS));
        loot.addProvider(output -> new LootEnchantmentTagGenerator(output, lookup, existingFileHelper));
        loot.addProvider(output -> new AdvancementProvider(output, lookup, List.of(new LootAdvancementGenerator())));

        // guaranteed_drops
        var drops = builtinPack(generator, PenchantModules.GUARANTEED_DROPS);
        drops.addProvider(output -> new FlagTagGenerator(output, lookup, existingFileHelper,
                PenchantFlag.GUARANTEED_ENCHANTED_DROP, PenchantFlag.GUARANTEED_TRIDENT_DROP));

        // reduced_curses
        var curses = builtinPack(generator, PenchantModules.REDUCED_CURSES);
        curses.addProvider(output -> new CurseEnchantmentTagGenerator(output, lookup, existingFileHelper));

        // randomized_librarians disabled on 1.21.1 (no VILLAGER_TRADE registry)
    }

    private static DataGenerator.PackGenerator builtinPack(DataGenerator generator, ResourceLocation id) {
        var pack = generator.getPackGenerator(true, id.getPath(), "resourcepacks/" + id.getPath());
        pack.addProvider(PackMetaGen.pack(id));
        return pack;
    }
}
