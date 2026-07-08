package archives.tater.penchant.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client configuration, backed by NeoForge's {@link ModConfigSpec} (replaces the Fabric-only kaleido config).
 */
public final class PenchantClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ALWAYS_SHOW_TOOLTIP_PROGRESS;
    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIP_KEY_HINT;
    public static final ModConfigSpec.IntValue BAR_WIDTH;

    static {
        var builder = new ModConfigSpec.Builder();

        ALWAYS_SHOW_TOOLTIP_PROGRESS = builder
                .comment("Whether enchantment progress should always be shown regardless of keypress")
                .define("alwaysShowTooltipProgress", false);

        SHOW_TOOLTIP_KEY_HINT = builder
                .comment("If the hint for showing enchantment progress with keybind should be shown",
                        "Does nothing if Always Show Progress is enabled")
                .define("showTooltipKeyHint", true);

        BAR_WIDTH = builder
                .comment("Width of enchantment progress bar")
                .defineInRange("barWidth", 32, 4, 128);

        SPEC = builder.build();
    }

    private PenchantClientConfig() {}
}
