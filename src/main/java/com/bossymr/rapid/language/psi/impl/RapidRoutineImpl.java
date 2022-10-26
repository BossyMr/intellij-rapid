package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class RapidRoutineImpl extends RapidStubElement<RapidRoutineStub> implements RapidRoutine {

    public RapidRoutineImpl(@NotNull RapidRoutineStub stub) {
        super(stub, RapidStubElementTypes.ROUTINE);
    }

    public RapidRoutineImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRoutine(this);
    }

    @Override
    public boolean isLocal() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.isLocal();
        } else {
            return findChildByType(RapidTokenTypes.LOCAL_KEYWORD) != null;
        }
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            PsiElement node = findNotNullChildByType(Attribute.TOKEN_SET);
            return Objects.requireNonNull(Attribute.getAttribute(node.getNode().getElementType()));
        }
    }

    @Override
    public @Nullable RapidType getType() {
        // TODO: 2022-10-26 Get type using stub
        return getTypeElement() != null ? getTypeElement().getType() : null;
    }

    @Override
    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable RapidParameterList getParameterList() {
        return getStubOrPsiChild(RapidStubElementTypes.PARAMETER_LIST);
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.FIELD, new RapidField[0]));
    }

    @Override
    public @NotNull RapidStatementList getStatementList() {
        return Objects.requireNonNull(getStatementList(RapidStatementList.Attribute.STATEMENT_LIST));
    }

    @Override
    public @Nullable RapidStatementList getBackwardStatementList() {
        return getStatementList(RapidStatementList.Attribute.BACKWARD_CLAUSE);
    }

    @Override
    public @Nullable RapidStatementList getErrorStatementList() {
        return getStatementList(RapidStatementList.Attribute.ERROR_CLAUSE);
    }

    @Override
    public @Nullable RapidStatementList getUndoStatementList() {
        return getStatementList(RapidStatementList.Attribute.UNDO_CLAUSE);
    }

    private @Nullable RapidStatementList getStatementList(@NotNull RapidStatementList.Attribute attribute) {
        List<RapidStatementList> statementLists = findChildrenByType(RapidElementTypes.STATEMENT_LIST);
        for (RapidStatementList statementList : statementLists) {
            if (statementList.getAttribute().equals(attribute)) {
                return statementList;
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
        final NamedStub<?> stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        } else {
            PsiElement identifier = getNameIdentifier();
            return identifier != null ? identifier.getText() : null;
        }
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "RapidRoutine:" + getName();
    }
}
