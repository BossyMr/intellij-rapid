package com.bossymr.rapid.ide.editor;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

public class RapidCommenter implements Commenter {

    @Override
    public @Nullable String getLineCommentPrefix() {
        return "!";
    }

    @Override
    public @Nullable String getBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getBlockCommentSuffix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }
}
