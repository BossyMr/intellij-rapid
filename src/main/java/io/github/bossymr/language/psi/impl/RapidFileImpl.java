package io.github.bossymr.language.psi.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.bossymr.language.RapidFileType;
import io.github.bossymr.language.RapidLanguage;
import io.github.bossymr.language.psi.RapidFile;
import io.github.bossymr.language.psi.RapidModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidFileImpl extends PsiFileBase implements RapidFile {

    public RapidFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RapidLanguage.INSTANCE);
    }

    @Override
    public @NotNull List<@NotNull RapidModule> getModules() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, RapidModule.class);
    }

    @Override
    public @NotNull FileType getFileType() {
        return RapidFileType.INSTANCE;
    }
}
