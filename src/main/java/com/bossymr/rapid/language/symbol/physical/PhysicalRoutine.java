package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class PhysicalRoutine extends RapidStubElement<RapidRoutineStub> implements RapidRoutine, PhysicalSymbol {

    public PhysicalRoutine(@NotNull RapidRoutineStub stub) {
        super(stub, RapidStubElementTypes.ROUTINE);
    }

    public PhysicalRoutine(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable Icon getIcon(int flags) {
        return getIcon();
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRoutine(this);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.getVisibility();
        } else {
            return Visibility.getVisibility(this);
        }
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            return Attribute.getAttribute(this);
        }
    }

    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable RapidType getType() {
        return SymbolUtil.getType(this);
    }

    public @Nullable RapidParameterList getParameterList() {
        return getStubOrPsiChild(RapidStubElementTypes.PARAMETER_LIST);
    }

    @Override
    public @Nullable List<RapidParameterGroup> getParameters() {
        RapidParameterList parameterList = getParameterList();
        return parameterList != null ? parameterList.getParameters() : null;
    }

    public @NotNull RapidFieldList getFieldList() {
        return findNotNullChildByType(RapidElementTypes.FIELD_LIST);
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return getFieldList().getFields();
    }

    @Override
    public @NotNull List<RapidStatement> getStatements() {
        List<RapidStatement> statements = getStatements(RapidStatementList.Attribute.STATEMENT_LIST);
        assert statements != null;
        return statements;
    }

    @Override
    public @Nullable List<RapidStatement> getStatements(@NotNull RapidStatementList.Attribute attribute) {
        List<RapidStatementList> statementLists = findChildrenByType(RapidElementTypes.STATEMENT_LIST);
        for (RapidStatementList statementList : statementLists) {
            if (attribute == statementList.getAttribute()) {
                return statementList.getStatements();
            }
        }
        return null;
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
        return this;
    }

    @Override
    public String toString() {
        return "PhysicalRoutine:" + getName();
    }
}
