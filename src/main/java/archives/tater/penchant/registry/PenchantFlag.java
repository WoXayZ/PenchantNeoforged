package archives.tater.penchant.registry;

/**
 * Runtime feature flags. Defaults match the built-in datapack modules enabled for 1.4.
 */
public enum PenchantFlag {
    REWORKED_TABLE_MENU(true),
    LENIENT_BOOKSHELF_PLACEMENT(true),
    NO_ANVIL_BOOKS(true),
    LOOT_REWORK(true),
    GUARANTEED_ENCHANTED_DROP(true),
    GUARANTEED_TRIDENT_DROP(true);

    private final boolean defaultEnabled;
    private boolean enabled;

    PenchantFlag(boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
        this.enabled = defaultEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reset() {
        this.enabled = defaultEnabled;
    }
}
