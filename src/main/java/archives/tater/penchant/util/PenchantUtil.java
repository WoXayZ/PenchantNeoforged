package archives.tater.penchant.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class PenchantUtil {
    private PenchantUtil() {}

    public static <T> Stream<Holder<T>> streamOrdered(HolderLookup<T> registry, TagKey<T> orderTag) {
        return registry.get(orderTag).map(holderSet -> Stream.concat(
                holderSet.stream(),
                registry.listElements()
                        .filter(holder -> !holderSet.contains(holder))
        )).orElseGet(() -> registry.listElements().map(Function.identity()));
    }

    public static List<String> toFlatListRemoveStyle(Component text) {
        var result = new ArrayList<String>();
        text.visit((style, contents) -> {
            if (!contents.isEmpty())
                result.add(contents);
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
