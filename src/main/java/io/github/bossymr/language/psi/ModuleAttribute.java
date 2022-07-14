package io.github.bossymr.language.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.github.bossymr.language.psi.RapidTokenTypes.*;

/**
 * Represents the possible attributes which can be declared on a module.
 */
public enum ModuleAttribute {
    /**
     * The module is a system module.
     */
    SYSTEM_MODULE(SYSMODULE_KEYWORD, "SYSMODULE"),

    /**
     * The module source code is not viewable.
     */
    NO_VIEW(NOVIEW_KEYWORD, "NOVIEW"),

    /**
     * The module cannot be stepped into during stepwise execution.
     */
    NO_STEP_IN(NOSTEPIN_KEYWORD, "NOSTEPIN"),

    /**
     * The module cannot be modified.
     */
    VIEW_ONLY(VIEWONLY_KEYWORD, "VIEWONLY"),

    /**
     * The module cannot be modified, but this attribute can be removed.
     */
    READ_ONLY(READONLY_KEYWORD, "READONLY");


    private static final Map<IElementType, ModuleAttribute> TYPE_TO_ATTRIBUTE;

    static {
        Map<IElementType, ModuleAttribute> MODIFIABLE_MAP = new HashMap<>();
        for (ModuleAttribute value : values()) {
            MODIFIABLE_MAP.put(value.getElementType(), value);
        }
        TYPE_TO_ATTRIBUTE = Collections.unmodifiableMap(MODIFIABLE_MAP);
    }

    private final IElementType keyword;
    private final String text;

    ModuleAttribute(IElementType keyword, String text) {
        this.keyword = keyword;
        this.text = text;
    }

    /**
     * Returns the attribute associated to the specified element type.
     *
     * @param elementType the element type to search for,
     * @return the module attribute associated with the specified element type, or {@code null} if the specified element
     * type is not associated with any attribute.
     */
    public static @Nullable ModuleAttribute getAttribute(@NotNull IElementType elementType) {
        return TYPE_TO_ATTRIBUTE.get(elementType);
    }

    public IElementType getElementType() {
        return keyword;
    }

    public @NotNull String getText() {
        return text;
    }
}
