package io.github.bossymr.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.bossymr.RapidBundle;
import io.github.bossymr.RapidIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class RapidFileType extends LanguageFileType {

    public static final @NonNls String DEFAULT_EXTENSION = "mod";
    public static final @NonNls String DOT_DEFAULT_EXTENSION = ".mod";

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
    public @Nullable Icon getIcon() {
        return RapidIcons.FileTypes.Rapid;
    }

    @Override
    public @NonNls @Nullable String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
        return StandardCharsets.ISO_8859_1.name();
    }
}
