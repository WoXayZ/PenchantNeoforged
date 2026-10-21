package archives.tater.penchant;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * World gameplay options. Stored with the world and synced to clients, so a change applies to that world
 * immediately, including worlds created before the option existed.
 */
public final class PenchantGameplayConfig {
    public static final String FILE_NAME = Penchant.MOD_ID + "/gameplay.toml";

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue PROGRESSION_MULTIPLIER;

    static {
        var builder = new ModConfigSpec.Builder();

        builder.comment(
                "How fast enchantments level up from use.",
                "Modpacks can ship a default in defaultconfigs/penchant/gameplay.toml.",
                "The value is saved with the world and synced to clients, so editing it changes existing worlds."
        ).push("progression");

        PROGRESSION_MULTIPLIER = builder
                .comment(
                        "Multiplies how much use an enchantment needs before it levels up.",
                        "1 keeps the default speed. 2 makes leveling twice as slow. 0.5 makes it twice as fast.",
                        "Stacks with an enchantment's datapack progress_cost_factor and any per-item cost factor."
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
