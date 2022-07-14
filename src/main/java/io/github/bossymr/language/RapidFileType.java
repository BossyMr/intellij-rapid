package io.github.bossymr.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.bossymr.RapidIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class RapidFileType extends LanguageFileType {

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
        return "Rapid";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "mod";
    }

    @Override
    public @Nullable Icon getIcon() {
        return RapidIcons.RAPID_FILE;
    }

    @Override
    public @NonNls @Nullable String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
        return StandardCharsets.ISO_8859_1.name();
    }
}
