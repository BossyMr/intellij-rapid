package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PhysicalRoutine extends RapidStubElement<RapidRoutineStub> implements RapidRoutine, PhysicalSymbol, PhysicalVisibleSymbol {

    public PhysicalRoutine(@NotNull RapidRoutineStub stub) {
        super(stub, RapidStubElementTypes.ROUTINE);
    }

    public PhysicalRoutine(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Returns the parent routine of the specified element. If the element is a routine, it is returned.
     *
     * @param element the element.
     * @return the parent routine of the element, or {@code null} if the element has no parent routine.
     * @see PhysicalModule#getModule(PsiElement)
     */
    public static @Nullable PhysicalRoutine getRoutine(@NotNull PsiElement element) {
        if (element instanceof PhysicalRoutine routine) {
            return routine;
        }
        return PsiTreeUtil.getParentOfType(element, PhysicalRoutine.class);
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
    public @NotNull RoutineType getRoutineType() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.getRoutineType();
        } else {
            return RoutineType.getAttribute(this);
        }
    }

    @Override
    public @Nullable RapidType getType() {
        return SymbolUtil.getType(this);
    }

    public @Nullable RapidParameterList getParameterList() {
        return getStubOrPsiChild(RapidStubElementTypes.PARAMETER_LIST);
    }

    @Override
    public @Nullable List<PhysicalParameterGroup> getParameters() {
        RapidParameterList parameterList = getParameterList();
        return parameterList != null ? parameterList.getParameters() : null;
    }

    @Override
    public @NotNull List<PhysicalField> getFields() {
        return PsiTreeUtil.getChildrenOfTypeAsList(getStatementList(), PhysicalField.class);
    }

    @Override
    public @NotNull List<RapidStatement> getStatements() {
        return getStatementList().getStatements();
    }

    public @NotNull RapidStatementList getStatementList() {
        RapidStatementList statementList = getStatementList(StatementListType.STATEMENT_LIST);
        return Objects.requireNonNull(statementList);
    }

    public @NotNull List<RapidStatementList> getStatementLists() {
        return findChildrenByType(RapidElementTypes.STATEMENT_LIST);
    }

    @Override
    public @Nullable List<RapidStatement> getStatements(@NotNull StatementListType statementListType) {
        RapidStatementList statementList = getStatementList(statementListType);
        return statementList != null ? statementList.getStatements() : null;
    }

    public @Nullable RapidStatementList getStatementList(@NotNull StatementListType statementListType) {
        List<RapidStatementList> statementLists = findChildrenByType(RapidElementTypes.STATEMENT_LIST);
        for (RapidStatementList statementList : statementLists) {
            if (statementListType == statementList.getStatementListType()) {
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
        return SymbolUtil.getName(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalRoutine> createPointer() {
        return new PhysicalPointer<>(this);
    }

    @Override
    public String toString() {
        return "PhysicalRoutine{" +
                "name='" + getName() + '\'' +
                '}';
    }
}
