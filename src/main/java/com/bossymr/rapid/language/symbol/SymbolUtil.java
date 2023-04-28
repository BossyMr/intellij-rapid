package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.psi.RapidTypeStub;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidVisibleStub;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SymbolUtil {

    private SymbolUtil() {
    }

    public static @NotNull TextRange getDeclaration(@NotNull PhysicalRecord record) {
        int startOffset, endOffset;
        startOffset = record.getTextRange().getStartOffset();
        PsiElement nameIdentifier = record.getNameIdentifier();
        if (nameIdentifier != null) {
            endOffset = nameIdentifier.getTextRange().getEndOffset();
        } else {
            ASTNode keyword = record.getNode().findChildByType(RapidTokenTypes.RECORD_KEYWORD);
            assert keyword != null;
            endOffset = keyword.getStartOffset();
        }
        return TextRange.create(startOffset, endOffset);
    }

    public static @NotNull TextRange getDeclaration(@NotNull PhysicalRoutine routine) {
        int startOffset, endOffset;
        startOffset = routine.getTextRange().getStartOffset();
        if (routine.getParameterList() != null) {
            endOffset = routine.getParameterList().getTextRange().getEndOffset();
        } else {
            PsiElement nameIdentifier = routine.getNameIdentifier();
            if (nameIdentifier != null) {
                endOffset = nameIdentifier.getTextRange().getEndOffset();
            } else {
                ASTNode keyword = routine.getNode().findChildByType(TokenSet.create(RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.TRAP_KEYWORD));
                assert keyword != null;
                endOffset = keyword.getStartOffset();
            }
        }
        return TextRange.create(startOffset, endOffset);
    }

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

    public static <T extends RapidStubElement<? extends RapidVisibleStub>> @NotNull Visibility getVisibility(T element) {
        RapidVisibleStub stub = element.getGreenStub();
        if (stub != null) {
            return stub.getVisibility();
        } else {
            return Visibility.getVisibility(element);
        }
    }

    public static <T extends RapidStubElement<? extends RapidTypeStub>> @Nullable RapidType getType(@NotNull T element) {
        return getType(element, 0);
    }


    public static <T extends RapidStubElement<? extends RapidTypeStub>> @Nullable RapidType getType(@NotNull T element, int dimensions) {
        RapidTypeStub stub = element.getGreenStub();
        if (stub != null) {
            String name = stub.getType();
            if (name == null) return null;
            List<RapidSymbol> symbols = RapidResolveService.getInstance(element.getProject()).findSymbols(element, name);
            RapidSymbol symbol = symbols.size() > 0 ? symbols.get(0) : null;
            RapidStructure structure = symbol instanceof RapidStructure result ? result : null;
            return new RapidType(structure, name, stub.getDimensions());
        } else {
            RapidTypeElement typeElement = PsiTreeUtil.getChildOfType(element, RapidTypeElement.class);
            RapidType type = typeElement != null ? typeElement.getType() : null;
            if (type != null) {
                if (dimensions > 0) {
                    type = type.createArrayType(dimensions);
                }
            }
            return type;
        }

    }
}
