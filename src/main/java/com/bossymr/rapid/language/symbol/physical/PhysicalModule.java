package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidModuleStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Stream;

public class PhysicalModule extends RapidStubElement<RapidModuleStub> implements RapidModule, PhysicalSymbol {

    public PhysicalModule(@NotNull RapidModuleStub stub) {
        super(stub, RapidStubElementTypes.MODULE);
    }

    public PhysicalModule(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable Icon getIcon(int flags) {
        return getIcon();
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitModule(this);
    }

    public @Nullable RapidAttributeList getAttributeList() {
        return getStubOrPsiChild(RapidStubElementTypes.ATTRIBUTE_LIST);
    }

    @Override
    public @NotNull List<Attribute> getAttributes() {
        RapidAttributeList attributeList = getAttributeList();
        return attributeList != null ? attributeList.getAttributes() : Collections.emptyList();
    }

    @Override
    public boolean hasAttribute(@NotNull Attribute attribute) {
        RapidAttributeList attributeList = getAttributeList();
        return attributeList != null && attributeList.hasAttribute(attribute);
    }

    @Override
    public @NotNull List<RapidAccessibleSymbol> getSymbols() {
        return Stream.of(getStructures(), getFields(), getRoutines())
                .flatMap(Collection::stream)
                .map(symbol -> (RapidAccessibleSymbol) symbol)
                .toList();
    }

    @Override
    public @NotNull List<RapidStructure> getStructures() {
        RapidRecord[] records = getStubOrPsiChildren(RapidStubElementTypes.RECORD, new PhysicalRecord[0]);
        RapidAlias[] aliases = getStubOrPsiChildren(RapidStubElementTypes.ALIAS, new PhysicalAlias[0]);
        return Stream.of(records, aliases)
                .flatMap(Arrays::stream)
                .toList();

    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.FIELD, new PhysicalField[0]));
    }

    @Override
    public @NotNull List<RapidRoutine> getRoutines() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.ROUTINE, new PhysicalRoutine[0]));
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        return SymbolUtil.getName(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        String previousName = this.getName();
        PsiFile containingFile = getContainingFile();
        VirtualFile virtualFile = getContainingFile().getVirtualFile();
        if (virtualFile != null) {
            String fileName = virtualFile.getNameWithoutExtension();
            if (fileName.equals(previousName)) {
                String extension = virtualFile.getExtension();
                containingFile.setName(name + "." + extension);
            }
        }
        return this;
    }

    @Override
    public @Nullable ASTNode addInternal(@Nullable ASTNode first, @Nullable ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (!(first instanceof TreeElement)) return null;
        if (anchor == null) {
            if (first != last) {
                anchor = findChildByType(RapidTokenTypes.ENDMODULE_KEYWORD);
                before = true;
            }
            IElementType elementType = first.getElementType();
            if (TokenSet.create(RapidElementTypes.ALIAS, RapidElementTypes.RECORD).contains(elementType)) {
                if (before == null || before) {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.FIELD, RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                } else {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.ALIAS, RapidElementTypes.RECORD, RapidElementTypes.FIELD, RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                }
                before = true;
            } else if (TokenSet.create(RapidElementTypes.FIELD).contains(elementType)) {
                if (before == null || before) {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                } else {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.FIELD, RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                }
                before = true;
            } else if (TokenSet.create(RapidElementTypes.ROUTINE).contains(elementType)) {
                if (before == null || before) {
                    anchor = findChildByType(RapidTokenTypes.ENDMODULE_KEYWORD);
                } else {
                    anchor = findChildByType(TokenSet.create(RapidElementTypes.ROUTINE, RapidTokenTypes.ENDMODULE_KEYWORD));
                }
                before = true;
            }
        }
        return super.addInternal(first, last, anchor, before);
    }

    @Override
    public String toString() {
        return "PhysicalModule:" + this.getName();
    }
}
