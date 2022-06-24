package io.github.bossymr.language.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import io.github.bossymr.language.RapidFileType;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NotNull;

public class RapidFile extends PsiFileBase {

    public RapidFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RapidLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return RapidFileType.INSTANCE;
    }
}
