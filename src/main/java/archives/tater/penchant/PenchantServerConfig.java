package archives.tater.penchant;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Which modules are pre-selected for newly created worlds, backed by NeoForge's {@link ModConfigSpec}
 * (replaces the Fabric-only kaleido config).
 *
 * <p>Loaded as a startup config because the module datapacks are registered before any world exists.
 * Worlds keep whatever selection they were created with, so changing this never touches an existing save.
 */
public final class PenchantServerConfig {
    public static final String FILE_NAME = Penchant.MOD_ID + "/server.toml";

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DURABILITY_REWORK;
    public static final ModConfigSpec.BooleanValue BOOKSHELF_PLACEMENT;
    public static final ModConfigSpec.BooleanValue TABLE_REWORK;
    public static final ModConfigSpec.BooleanValue NO_ANVIL_BOOKS;
    public static final ModConfigSpec.BooleanValue LOOT_REWORK;
    public static final ModConfigSpec.BooleanValue GUARANTEED_DROPS;
    public static final ModConfigSpec.BooleanValue RANDOMIZED_LIBRARIANS;

    static {
        var builder = new ModConfigSpec.Builder();

        builder.comment(
                "Which modules should be enabled by default for new worlds.",
                "Note this can still be overridden per-world and will not affect existing worlds.",
                "This config is mainly intended for modpack developers. If you are an end user, prefer adjusting",
                "modules per-world using the vanilla datapack menu."
        ).push("moduleDefaults");

        DURABILITY_REWORK = builder
                .comment("Remove mending & alter unbreaking")
                .define("durabilityRework", true);

        BOOKSHELF_PLACEMENT = builder
                .comment("Larger bookshelf radius & allow chiseled bookshelves")
                .define("bookshelfPlacement", true);

        TABLE_REWORK = builder
                .comment("Enchanting via chiseled bookshelves & remove table RNG")
                .define("tableRework", true);

        NO_ANVIL_BOOKS = builder
                .comment("Prevent applying enchanted books to equipment in anvil")
                .define("noAnvilBooks", true);

        LOOT_REWORK = builder
                .comment("Reorganize where enchantments are unlocked & found")
                .define("lootRework", true);

        GUARANTEED_DROPS = builder
                .comment("Mobs always drop enchanted equipment")
                .define("guaranteedDrops", true);

        RANDOMIZED_LIBRARIANS = builder
                .comment("Librarians sell a different book each time")
                .define("randomizedLibrarians", false);

        builder.pop();

        SPEC = builder.build();
    }

    private PenchantServerConfig() {}
}
