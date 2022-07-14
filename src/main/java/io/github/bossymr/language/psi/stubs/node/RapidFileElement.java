package io.github.bossymr.language.psi.stubs.node;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.FileElement;
import io.github.bossymr.language.parser.RapidParserDefinition;
import io.github.bossymr.language.psi.RapidElementTypes;
import io.github.bossymr.language.psi.RapidFile;
import org.jetbrains.annotations.NotNull;

public class RapidFileElement extends FileElement {
    public RapidFileElement(CharSequence text) {
        super(RapidParserDefinition.FILE, text);
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (child.getElementType().equals(RapidElementTypes.MODULE)) {
            RapidFile file = SourceTreeToPsiMap.treeToPsiNotNull(this);
            if (file.getModules().size() < 2) {
                file.delete();
                return;
            }
        }
        super.deleteChildInternal(child);
    }
}
