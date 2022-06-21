package io.github.bossymr.language.core.lexer;

import com.intellij.lexer.FlexAdapter;

public class RapidLexer extends FlexAdapter {

    public RapidLexer() {
        super(new _RapidLexer());
    }
}
