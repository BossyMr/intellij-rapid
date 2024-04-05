package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.RapidBundle;
import org.jetbrains.annotations.NotNull;

public class RapidWithIfElseSurrounder extends RapidStatementSurrounder {

    @Override
    public String getTemplateDescription() {
        return RapidBundle.message("surrounder.with.if.else");
    }

    @Override
    protected @NotNull String getPrefix() {
        return "IF";
    }

    @Override
    protected @NotNull String getSuffix() {
        return "THEN\nELSE\nENDIF";
    }
}
