package io.github.bossymr.language.psi;

import com.intellij.psi.tree.IElementType;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RapidElementType extends IElementType {

    public RapidElementType(@NonNls @NotNull String debugName) {
        super(debugName, RapidLanguage.INSTANCE);
    }
}
