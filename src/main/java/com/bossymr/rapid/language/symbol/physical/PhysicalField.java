package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidFieldStub;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalField extends RapidStubElement<RapidFieldStub> implements RapidField, PhysicalVariable, PhysicalVisibleSymbol {

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
        return SymbolUtil.getVisibility(this);
    }

    @Override
    public @NotNull FieldType getFieldType() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            return FieldType.getAttribute(this);
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
            return elementFactory.createExpressionFromText(initializer);
        } else {
            return findInitializer();
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

    public @Nullable RapidExpression findInitializer() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
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

    @Override
    public @Nullable RapidType getType() {
        RapidArray array = getArray();
        int dimensions = array != null ? array.getDimensions().size() : 0;
        return SymbolUtil.getType(this, dimensions);
    }

    public @Nullable RapidArray getArray() {
        return findChildByType(RapidElementTypes.ARRAY);
    }

    @Override
    public String getName() {
        return SymbolUtil.getName(this);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalField> createPointer() {
        return new PhysicalPointer<>(this);
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (RapidElementTypes.EXPRESSIONS.contains(child.getElementType())) {
            ASTNode equals = findChildByType(RapidTokenTypes.CEQ);
            if (equals != null) {
                deleteChildInternal(equals);
            }
        }
        super.deleteChildInternal(child);
    }


    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "PhysicalField{" +
               "name='" + getName() + '\'' +
               '}';
    }
}
