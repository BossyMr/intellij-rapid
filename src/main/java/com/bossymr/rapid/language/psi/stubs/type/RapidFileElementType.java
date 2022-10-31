package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.stubs.RapidFileStub;
import com.bossymr.rapid.language.psi.stubs.node.RapidFileElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.LightStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.ILightStubFileElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidFileElementType extends ILightStubFileElementType<RapidFileStub> {
    public static final int STUB_VERSION = 1;

    public RapidFileElementType() {
        super("rapid.FILE", RapidLanguage.INSTANCE);
    }

    @Override
    public @Nullable ASTNode createNode(CharSequence text) {
        return new RapidFileElement(text);
    }

    @Override
    public LightStubBuilder getBuilder() {
        return new LightStubBuilder() {
            @Override
            protected @NotNull StubElement<?> createStubForFile(@NotNull PsiFile file, @NotNull LighterAST tree) {
                if (!(file instanceof RapidFile)) {
                    return super.createStubForFile(file, tree);
                }
                return new RapidFileStub((RapidFile) file);
            }
        };
    }

    @Override
    public int getStubVersion() {
        return STUB_VERSION;
    }
}
