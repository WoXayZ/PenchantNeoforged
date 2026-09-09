package archives.tater.penchant;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Which modules are pre-selected for newly created worlds.
 *
 * <p>Worlds keep whatever selection they were created with, so changing this never touches an existing save.
 */
public final class PenchantServerConfig {
    public static final String FILE_NAME = Penchant.MOD_ID + "/server.toml";

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DURABILITY_REWORK;
    public static final ForgeConfigSpec.BooleanValue BOOKSHELF_PLACEMENT;
    public static final ForgeConfigSpec.BooleanValue TABLE_REWORK;
    public static final ForgeConfigSpec.BooleanValue NO_ANVIL_BOOKS;
    public static final ForgeConfigSpec.BooleanValue LOOT_REWORK;
    public static final ForgeConfigSpec.BooleanValue GUARANTEED_DROPS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

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

        builder.pop();

        SPEC = builder.build();
    }

    private PenchantServerConfig() {}
}
