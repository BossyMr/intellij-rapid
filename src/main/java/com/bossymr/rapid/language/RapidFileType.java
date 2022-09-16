package com.bossymr.rapid.language;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class RapidFileType extends LanguageFileType {

    public static final String DEFAULT_EXTENSION = "mod";
    public static final String DEFAULT_DOT_EXTENSION = ".mod";

    public static final RapidFileType INSTANCE = new RapidFileType();

    protected RapidFileType() {
        super(RapidLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "RAPID";
    }

    @Override
    public @NotNull String getDescription() {
        return RapidBundle.message("filetype.rapid.description");
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return RapidIcons.RAPID_FILE;
    }

    @Override
    public @NonNls @Nullable String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
        return StandardCharsets.ISO_8859_1.name();
    }
}
