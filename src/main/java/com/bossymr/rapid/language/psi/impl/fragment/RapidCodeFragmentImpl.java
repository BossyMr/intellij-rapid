package com.bossymr.rapid.language.psi.impl.fragment;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidCodeFragment;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.impl.AbstractRapidFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidCodeFragmentImpl extends AbstractRapidFile implements RapidCodeFragment, IntentionFilterOwner {

    private final @Nullable PsiElement context;
    private final boolean isPhysical;
    private @Nullable GlobalSearchScope resolveScope;
    private @Nullable IntentionActionsFilter intentionActionsFilter;

    protected RapidCodeFragmentImpl(@NotNull Project project,
                                    @NotNull IElementType elementType,
                                    boolean isPhysical,
                                    @NotNull @NonNls String fileName,
                                    @NotNull String text,
                                    @Nullable PsiElement context) {
        super(PsiManagerEx.getInstanceEx(project).getFileManager().createFileViewProvider(new LightVirtualFile(fileName, RapidLanguage.getInstance(), text), isPhysical));
        super.init(TokenType.CODE_FRAGMENT, elementType);
        getViewProvider().forceCachedPsi(this);
        this.context = context;
        this.isPhysical = isPhysical;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RapidElementVisitor elementVisitor) {
            elementVisitor.visitCodeFragment(this);
        } else {
            visitor.visitFile(this);
        }
    }

    @Override
    public @Nullable PsiElement getContext() {
        return context != null && context.isValid() ? context : super.getContext();
    }

    @Override
    public @NotNull SingleRootFileViewProvider getViewProvider() {
        return (SingleRootFileViewProvider) super.getViewProvider();
    }


    @Override
    public boolean isPhysical() {
        return isPhysical;
    }

    @Override
    public void forceResolveScope(GlobalSearchScope resolveScope) {
        this.resolveScope = resolveScope;
    }

    @Override
    public GlobalSearchScope getForcedResolveScope() {
        return resolveScope;
    }

    @Override
    public @Nullable IntentionActionsFilter getIntentionActionsFilter() {
        return intentionActionsFilter;
    }

    @Override
    public void setIntentionActionsFilter(@NotNull IntentionActionsFilter intentionActionsFilter) {
        this.intentionActionsFilter = intentionActionsFilter;
    }

    @Override
    public String toString() {
        return "RapidCodeFragment:" + getName();
    }
}
