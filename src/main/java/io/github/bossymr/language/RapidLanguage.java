package io.github.bossymr.language;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public class RapidLanguage extends Language {

    public static final RapidLanguage INSTANCE = new RapidLanguage();

    protected RapidLanguage() {
        super("RAPID");
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Rapid";
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
}
