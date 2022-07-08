package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.RapidFile;
import org.jetbrains.annotations.NotNull;

public class RapidStubBuilder extends DefaultStubBuilder {

    @Override
    protected @NotNull StubElement<?> createStubForFile(@NotNull PsiFile file) {
        if(file instanceof RapidFile) {
            return new RapidFileStubImpl((RapidFile) file);
        }
        return super.createStubForFile(file);
    }
}
