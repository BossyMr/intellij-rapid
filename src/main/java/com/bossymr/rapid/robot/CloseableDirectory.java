package com.bossymr.rapid.robot;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class CloseableDirectory implements Closeable {

    private final @NotNull VirtualFile virtualFile;

    public CloseableDirectory(@NotNull String suffix) throws IOException {
        File file = FileUtil.createTempDirectory("intellij-rapid", suffix);
        File[] files = file.listFiles();
        if (files == null) {
            throw new IOException();
        }
        for (File child : files) {
            FileUtil.delete(child);
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (virtualFile == null) {
            throw new IOException("Failed to find directory '" + file + "'");
        }
        virtualFile.refresh(false, true);
        this.virtualFile = virtualFile;
    }

    public @NotNull VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public void close() throws IOException {
        WriteAction.runAndWait(() -> this.virtualFile.delete(this));
    }
}
