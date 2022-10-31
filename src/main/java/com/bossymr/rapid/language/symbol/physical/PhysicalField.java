package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidFieldStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalField extends RapidStubElement<RapidFieldStub> implements RapidField, PhysicalSymbol {

    public PhysicalField(@NotNull RapidFieldStub stub) {
        super(stub, RapidStubElementTypes.FIELD);
    }

    public PhysicalField(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitField(this);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getVisibility();
        } else {
            return Visibility.getVisibility(this);
        }
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            return Attribute.getAttribute(this);
        }
    }

    @Override
    public @Nullable RapidExpression getInitializer() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            String initializer = stub.getInitializer();
            if (initializer == null) {
                return null;
            }
            RapidElementFactory elementFactory = RapidElementFactory.getInstance(getProject());
            return elementFactory.createExpression(initializer);
        } else {
            return findChildByType(RapidElementTypes.EXPRESSIONS);
        }
    }

    @Override
    public void setInitializer(@Nullable RapidExpression expression) throws UnsupportedOperationException {
        RapidExpression previous = getInitializer();
        if (previous != null) {
            previous.delete();
        }
        if (expression == null) {
            return;
        }
        CompositeElement element = (CompositeElement) getNode();
        PsiElement equals = findChildByType(RapidTokenTypes.CEQ);
        if (equals == null) {
            CharTable charTable = SharedImplUtil.findCharTableByTree(element);
            TreeElement node = Factory.createSingleLeafElement(RapidTokenTypes.CEQ, ":=", charTable, getManager());
            ASTNode anchor = element.findChildByType(RapidTokenTypes.SEMICOLON);
            element.addInternal(node, node, anchor, Boolean.TRUE);
            assert findChildByType(RapidTokenTypes.CEQ) != null;
        }
        addAfter(expression, equals);
    }

    @Override
    public boolean hasInitializer() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getInitializer() != null;
        } else {
            return findChildByType(RapidElementTypes.EXPRESSIONS) != null;
        }
    }

    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }


    @Override
    public @Nullable RapidType getType() {
        return CachedValuesManager.getProjectPsiDependentCache(this, (ignored) -> {
            RapidFieldStub stub = getGreenStub();
            if (stub != null) {
                String typeName = stub.getType();
                if (typeName == null) return null;
                RapidStructure structure = ResolveUtil.getStructure(this, typeName);
                return new RapidType(structure, typeName, stub.getDimensions());
            } else {
                RapidType type = getTypeElement() != null ? getTypeElement().getType() : null;
                if (type != null) {
                    RapidArray array = findChildByType(RapidElementTypes.ARRAY);
                    int dimensions = array != null ? array.getDimensions().size() : 0;
                    if (dimensions > 0) {
                        type = type.createArrayType(dimensions);
                    }
                }
                return type;
            }
        });
    }

    @Override
    public String getName() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        } else {
            PsiElement identifier = getNameIdentifier();
            return identifier != null ? identifier.getText() : null;
        }
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "PhysicalField:" + getName();
    }
}
