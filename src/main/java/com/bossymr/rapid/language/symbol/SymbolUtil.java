package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidArray;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.psi.RapidTypeStub;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidVisibleStub;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SymbolUtil {

    private SymbolUtil() {}

    public static @Nullable String getName(@NotNull PhysicalSymbol element) {
        if (element instanceof RapidStubElement<?> stubElement) {
            StubElement<?> stub = stubElement.getGreenStub();
            if (stub instanceof NamedStub<?> namedStub) {
                return namedStub.getName();
            }
        }
        PsiElement identifier = element.getNameIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    public static @Nullable RapidModule getModule(@NotNull PsiElement element) {
        return PsiTreeUtil.getStubOrPsiParentOfType(element, PhysicalModule.class);
    }

    public static <T extends RapidStubElement<? extends RapidVisibleStub>> @NotNull Visibility getVisibility(@NotNull T element) {
        RapidVisibleStub stub = element.getGreenStub();
        if (stub != null) {
            return stub.getVisibility();
        } else {
            return Visibility.getVisibility(element);
        }
    }

    public static <T extends RapidStubElement<? extends RapidTypeStub>> @Nullable RapidType getType(@NotNull T element) {
        return getType(element, 0, null);
    }


    public static <T extends RapidStubElement<? extends RapidTypeStub>> @Nullable RapidType getType(@NotNull T element, int dimensions, @Nullable RapidArray array) {
        RapidTypeElement typeElement = PsiTreeUtil.getChildOfType(element, RapidTypeElement.class);
        RapidType type = typeElement != null ? typeElement.getType() : null;
        if (type != null) {
            if (dimensions > 0) {
                type = type.createArrayType(dimensions, array != null ? array.getDimensions() : null);
            }
        }
        return type;
    }
}
