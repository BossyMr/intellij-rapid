package io.github.bossymr.language;

import com.intellij.lang.Language;
import io.github.bossymr.RapidBundle;
import org.jetbrains.annotations.NotNull;

public class RapidLanguage extends Language {

    public static final RapidLanguage INSTANCE = new RapidLanguage();

    protected RapidLanguage() {
        super("RAPID");
    }

    @Override
    public @NotNull String getDisplayName() {
        return RapidBundle.message("language.rapid.display.name");
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
}
