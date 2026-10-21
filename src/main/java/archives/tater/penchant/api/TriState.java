package archives.tater.penchant.api;

/**
 * Three-way result. Forge 1.20.1 has no {@code TriState} of its own.
 */
public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }
}
