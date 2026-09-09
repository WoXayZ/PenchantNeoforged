package archives.tater.penchant.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class PenchantUtil {
    private PenchantUtil() {}

    public static List<String> toFlatListRemoveStyle(Component text) {
        List<String> result = new ArrayList<>();
        text.visit((style, contents) -> {
            if (!contents.isEmpty()) {
                result.add(contents);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    /**
     * Whether {@code check} appears inside {@code outer}, comparing only the text runs. Lets a tooltip
     * line be recognised after another mod has restyled or wrapped it.
     */
    public static boolean containsIgnoreStyle(Component outer, Component check) {
        return Collections.indexOfSubList(toFlatListRemoveStyle(outer), toFlatListRemoveStyle(check)) != -1;
    }
}
