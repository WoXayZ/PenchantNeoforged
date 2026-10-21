package archives.tater.penchant;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Gameplay options. Stored in {@code config/penchant/gameplay.toml} and synced from the server to clients,
 * so editing it changes how fast enchantments level on this server, including worlds that already exist.
 */
public final class PenchantGameplayConfig {
    public static final String FILE_NAME = Penchant.MOD_ID + "/gameplay.toml";

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue PROGRESSION_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "How fast enchantments level up from use.",
                "Modpacks can ship a default in defaultconfigs/penchant/gameplay.toml.",
                "The server syncs this file to clients, so editing it changes existing worlds."
        ).push("progression");

        PROGRESSION_MULTIPLIER = builder
                .comment(
                        "Multiplies how much use an enchantment needs before it levels up.",
                        "1 keeps the default speed. 2 makes leveling twice as slow. 0.5 makes it twice as fast.",
                        "Stacks with an enchantment's progress cost."
                )
                .defineInRange("multiplier", 1.0D, 0.05D, 1000.0D);

        builder.pop();
        SPEC = builder.build();
    }

    public static double progressionMultiplier() {
        return SPEC.isLoaded() ? PROGRESSION_MULTIPLIER.get() : 1.0D;
    }

    private PenchantGameplayConfig() {}
}
