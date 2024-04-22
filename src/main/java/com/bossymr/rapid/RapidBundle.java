package com.bossymr.rapid;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

/**
 * {@code RapidBundle} is used to retrieve messages which are safe to display.
 */
public class RapidBundle extends DynamicBundle {

    public static final String BUNDLE = "messages.RapidBundle";
    public static final RapidBundle INSTANCE = new RapidBundle();

    private RapidBundle() {
        super(BUNDLE);
    }

    /**
     * Returns the message associated with the specified key and parameters.
     *
     * @param key the message key.
     * @param parameters the message parameters.
     * @return the message.
     */
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... parameters) {
        return INSTANCE.getMessage(key, parameters);
    }

    /**
     * Returns a lazy message, which can be used to retrieve the message associated with the specified key and
     * parameters.
     *
     * @param key the message key.
     * @param parameters the message parameters.
     * @return the lazy message.
     */
    public static @NotNull Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... parameters) {
        return INSTANCE.getLazyMessage(key, parameters);
    }
}
