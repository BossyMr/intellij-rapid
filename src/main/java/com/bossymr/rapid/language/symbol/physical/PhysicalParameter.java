package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.ResolveUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalParameter extends RapidStubElement<RapidParameterStub> implements RapidParameter, PhysicalSymbol {

    public PhysicalParameter(@NotNull RapidParameterStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER);
    }

    public PhysicalParameter(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameter(this);
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidParameterStub stub = getGreenStub();
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
        return CachedValuesManager.getProjectPsiDependentCache(this, (ignored) -> {
            RapidParameterStub stub = getGreenStub();
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
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        RapidParameterStub stub = getGreenStub();
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
        return "PhysicalParameter:" + getName();
    }
}
