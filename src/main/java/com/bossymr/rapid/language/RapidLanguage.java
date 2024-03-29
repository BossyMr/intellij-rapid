package com.bossymr.rapid.language;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public class RapidLanguage extends Language {

    public static final RapidLanguage INSTANCE = new RapidLanguage();

    private RapidLanguage() {
        super("RAPID");
    }

    public static @NotNull RapidLanguage getInstance() {
        return INSTANCE;
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
