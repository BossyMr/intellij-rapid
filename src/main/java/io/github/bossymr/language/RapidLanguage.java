package io.github.bossymr.language;

import com.intellij.lang.Language;

public class RapidLanguage extends Language {

    public static final RapidLanguage INSTANCE = new RapidLanguage();

    protected RapidLanguage() {
        super("RAPID");
    }

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
}
