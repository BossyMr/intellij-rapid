package com.bossymr.rapid.ide.editor.insight.unwrap;

import com.intellij.codeInsight.unwrap.UnwrapDescriptorBase;
import com.intellij.codeInsight.unwrap.Unwrapper;

public class RapidUnwrapDescriptor extends UnwrapDescriptorBase {
    @Override
    protected Unwrapper[] createUnwrappers() {
        return new Unwrapper[]{
                new RapidIfUnwrapper(),
                new RapidForUnwrapper(),
                new RapidWhileUnwrapper()
        };
    }
}
