package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.PenchantServerConfig;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class PenchantModules {
    public static final Identifier DURABILITY_REWORK = Penchant.id("durability_rework");
    public static final Identifier BOOKSHELF_PLACEMENT = Penchant.id("bookshelf_placement");
    public static final Identifier TABLE_REWORK = Penchant.id("table_rework");
    public static final Identifier NO_ANVIL_BOOKS = Penchant.id("no_anvil_books");
    public static final Identifier LOOT_REWORK = Penchant.id("loot_rework");
    public static final Identifier GUARANTEED_DROPS = Penchant.id("guaranteed_drops");
    public static final Identifier RANDOMIZED_LIBRARIANS = Penchant.id("randomized_librarians");

    /**
     * @deprecated Folded into {@link #LOOT_REWORK} in 1.4 / upstream 0.5.0.
     */
    @Deprecated
    public static final Identifier REDUCED_CURSES = Penchant.id("reduced_curses");

    /** Off by default, toggleable, with no extra source decoration. */
    private static final PackSource NORMAL_SOURCE = PackSource.create(PackSource.NO_DECORATION, false);

    private static void registerPack(AddPackFindersEvent event, Identifier id, PackSource source) {
        // The pack contents ship in the mod jar under "resourcepacks/<id>". The namespace of the
        // pack location must be the mod id so NeoForge can resolve the owning mod file.
        event.addPackFinders(
                Penchant.id("resourcepacks/" + id.getPath()),
                PackType.SERVER_DATA,
                Component.translatable(id.toLanguageKey("dataPack", "name")),
                source,
                false,
                Pack.Position.TOP
        );
    }

    private static void registerPack(AddPackFindersEvent event, Identifier id) {
        registerPack(event, id, PackSource.BUILT_IN);
    }

    private static void registerPack(AddPackFindersEvent event, Identifier id, ModConfigSpec.BooleanValue defaultEnabled) {
        registerPack(event, id, defaultEnabled.get() ? PackSource.BUILT_IN : NORMAL_SOURCE);
    }

    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        registerPack(event, DURABILITY_REWORK, PenchantServerConfig.DURABILITY_REWORK);
        registerPack(event, BOOKSHELF_PLACEMENT, PenchantServerConfig.BOOKSHELF_PLACEMENT);
        registerPack(event, TABLE_REWORK, PenchantServerConfig.TABLE_REWORK);
        registerPack(event, NO_ANVIL_BOOKS, PenchantServerConfig.NO_ANVIL_BOOKS);
        registerPack(event, LOOT_REWORK, PenchantServerConfig.LOOT_REWORK);
        registerPack(event, GUARANTEED_DROPS, PenchantServerConfig.GUARANTEED_DROPS);
        registerPack(event, RANDOMIZED_LIBRARIANS, PenchantServerConfig.RANDOMIZED_LIBRARIANS);
    }

    public static void init() {

    }
}
