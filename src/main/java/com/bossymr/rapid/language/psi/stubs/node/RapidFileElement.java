package com.bossymr.rapid.language.psi.stubs.node;

import com.bossymr.rapid.language.parser.RapidParserDefinition;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidFile;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.FileElement;
import org.jetbrains.annotations.NotNull;

public class RapidFileElement extends FileElement {
    public RapidFileElement(CharSequence text) {
        super(RapidParserDefinition.FILE, text);
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (child.getElementType().equals(RapidElementTypes.MODULE)) {
            RapidFile file = SourceTreeToPsiMap.treeToPsiNotNull(this);
            if (file.getModules().size() == 1) {
                file.delete();
                return;
            }
        }
        super.deleteChildInternal(child);
    }
}
