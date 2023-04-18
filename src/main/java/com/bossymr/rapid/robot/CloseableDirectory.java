package com.bossymr.rapid.robot;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class CloseableDirectory implements Closeable {

    private final @NotNull File file;

    public CloseableDirectory(@NotNull String suffix) throws IOException {
        this.file = FileUtil.createTempDirectory("intellij-rapid", suffix);
        File[] files = file.listFiles();
        if (files == null) {
            throw new IOException();
        }
        for (File child : files) {
            FileUtil.delete(child);
        }
    }

    public @NotNull File getFile() {
        return file;
    }

    @Override
    public void close() {
        WriteAction.runAndWait(() -> FileUtil.delete(file));
    }
}
