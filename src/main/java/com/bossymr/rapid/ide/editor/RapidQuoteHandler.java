package com.bossymr.rapid.ide.editor;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;

public class RapidQuoteHandler extends SimpleTokenSetQuoteHandler {

    public RapidQuoteHandler() {
        super(RapidTokenTypes.STRING_LITERAL);
    }
}
