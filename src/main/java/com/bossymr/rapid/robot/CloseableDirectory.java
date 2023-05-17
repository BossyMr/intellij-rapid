package com.bossymr.rapid.robot;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.concurrency.annotations.RequiresWriteLock;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@code CloseableDirectory} is a directory which will be automatically deleted when this object is closed.
 * <p>
 * The underlying directory can be retrieved with {@link #getDirectory()}.
 */
public class CloseableDirectory implements Closeable {

    private final @NotNull File directory;

    /**
     * Creates a new {@code CloseableDirectory} which will delete the specified directory when closed.
     *
     * @param directory the directory.
     * @throws IllegalArgumentException if the specified file is not a {@link File#isDirectory() directory}.
     */
    public CloseableDirectory(@NotNull File directory) throws IllegalArgumentException {
        if (!(directory.isDirectory())) {
            throw new IllegalArgumentException("File: " + directory + " is not a directory");
        }
        this.directory = directory;
    }

    @RequiresWriteLock
    public CloseableDirectory(@NotNull String suffix) throws IOException {
        Path path = Files.createTempDirectory(suffix);
        this.directory = path.toFile();
        if (!(directory.isDirectory())) {
            throw new IOException("File: " + directory + " is not a directory");
        }
        /*
         * A previous issue was that, if a temporary directory was not deleted, and enough directories were created, it
         * would reuse an existing directory - which was not empty.
         */
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            Files.delete(file.toPath());
        }
    }

    public @NotNull File getDirectory() {
        return directory;
    }

    @Override
    public void close() {
        WriteAction.runAndWait(() -> FileUtil.delete(directory));
    }
}
