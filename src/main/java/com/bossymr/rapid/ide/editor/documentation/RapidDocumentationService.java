package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.RapidBundle;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.microsoft.chm.ChmCommons;
import org.apache.tika.parser.microsoft.chm.ChmDirectoryListingSet;
import org.apache.tika.parser.microsoft.chm.ChmExtractor;
import org.apache.tika.parser.microsoft.chm.DirectoryListingEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@State(name = "Rapid.Documentation", storages = @Storage(value = "RapidDocumentation.xml"))
public class RapidDocumentationService implements PersistentStateComponent<RapidDocumentationState> {

    public static final @NotNull String DOWNLOAD_PATH = "https://robotstudiocdn.azureedge.net/distributionpackages/RobotWare/ABB.RobotWareDoc.IRC5-6.15.rspak";
    private static final @NotNull String DOWNLOAD_FILE = "ABB.RobotWareDoc.IRC5-6.15.rspak";
    private static final @NotNull String INTERNAL_PATH = "ABB.RobotWareDoc.IRC5-6.15/Documentation";
    private static final @NotNull String FILE_PREFIX = "3HAC050917_TRM_RAPID_RW_6";

    private RapidDocumentationState state = new RapidDocumentationState();
    private volatile @NotNull RapidDocumentationService.State currentState = State.UNINITIALIZED;
    private @Nullable Map<String, Image> images;

    public static @NotNull RapidDocumentationService getInstance() {
        return ApplicationManager.getApplication().getService(RapidDocumentationService.class);
    }

    @Override
    public @NotNull RapidDocumentationState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull RapidDocumentationState state) {
        this.state = state;
    }

    public @NotNull @Unmodifiable Map<String, Image> getImages() {
        return Objects.requireNonNullElseGet(images, Map::of);
    }

    /**
     * Computes the external documentation for the symbol with the specified name. If external documentation has not
     * been downloaded, or is being downloaded by another thread, a notification is displayed and this method returns
     * {@code null} without blocking.
     *
     * @param project the current project.
     * @param name the name of the symbol.
     * @return the external documentation for the symbol, or {@code null} if documentation is not available.
     * @see #getImages()
     */
    public synchronized @Nullable String getDocumentation(@NotNull Project project, @NotNull String name) {
        if (currentState == State.DISABLED) {
            // Documentation is not available
            return null;
        }
        if (currentState == State.INITIALIZING) {
            // Documentation is being computed with a visible progress indicator, in the meantime,
            // documentation is not available.
            return null;
        }
        if (currentState == State.UNINITIALIZED) {
            // Documentation has not been initialized.
            currentState = State.INITIALIZING;
            if (!(prepareDocumentation(project, false))) {
                // Documentation is either being initialized or could not be initialized.
                return null;
            }
        }
        return getDocumentation(name);
    }

    /**
     * Finds the external documentation for the symbol with the specified name.
     *
     * @param name the name of the symbol.
     * @return the external documentation for the symbol, or {@code null} if documentation is not available.
     */
    private @Nullable String getDocumentation(@NotNull String name) {
        File file = getDocumentationPath().resolve(name + ".html").toFile();
        if (!(file.isFile())) {
            return null;
        }
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    private @NotNull Path getDocumentationPath() {
        return PathManager.getPluginsDir().resolve("Rapid").resolve("documentation");
    }

    /**
     * Finds the external documentation if already downloaded, or downloads and extracts it.
     *
     * @param project the current project.
     * @param reinitialize download external documentation even if documentation has already been downloaded.
     * @return {@code true} if the external documentation can be found immediately.
     */
    public boolean prepareDocumentation(@Nullable Project project, boolean reinitialize) {
        if (!reinitialize && retrieveExisting()) {
            currentState = State.INITIALIZED;
            return true;
        }
        Path documentationPath = getDocumentationPath();
        if (documentationPath.toFile().exists()) {
            // If an error was encountered while retrieving the existing documentation, the directory might still exist
            // but be corrupted. Just in case delete the directory and try downloading the documentation again.
            try {
                FileUtil.delete(documentationPath);
            } catch (IOException e) {
                currentState = State.DISABLED;
                return false;
            }
        }
        switch (state.downloadOption) {
            case ALWAYS -> retrieveDocumentation(project);
            case ASK -> promptForDownload(project);
            case NEVER -> currentState = State.DISABLED;
        }
        return false;
    }

    private boolean retrieveExisting() {
        Path documentationPath = getDocumentationPath();
        if (!(documentationPath.toFile().exists())) {
            return false;
        }
        try {
            this.images = processImages();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void promptForDownload(@Nullable Project project) {
        NotificationGroupManager.getInstance()
                                .getNotificationGroup("Documentation download")
                                .createNotification(RapidBundle.message("documentation.message.title"), RapidBundle.message("documentation.message.text"), NotificationType.INFORMATION)
                                .addAction(NotificationAction.createSimpleExpiring(RapidBundle.message("documentation.message.action.always"), () -> {
                                    state.downloadOption = RapidDocumentationState.DownloadOption.ALWAYS;
                                    retrieveDocumentation(project);
                                })).addAction(NotificationAction.createSimpleExpiring(RapidBundle.message("documentation.message.action.once"), () -> {
                                    state.downloadOption = RapidDocumentationState.DownloadOption.ASK;
                                    retrieveDocumentation(project);
                                })).addAction(NotificationAction.createSimpleExpiring(RapidBundle.message("documentation.message.action.never"), () ->
                                        state.downloadOption = RapidDocumentationState.DownloadOption.NEVER))
                                .notify(project);
    }

    private void retrieveDocumentation(@Nullable Project project) {
        new Task.Backgroundable(project, RapidBundle.message("documentation.task.title"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    long startTime = System.currentTimeMillis();
                    Path documentationPath = getDocumentationPath();
                    if (documentationPath.toFile().exists()) {
                        FileUtil.delete(documentationPath);
                    }
                    File file = download();
                    indicator.setText(RapidBundle.message("documentation.extract.zip"));
                    indicator.setText2(null);
                    try (ZipFile zipFile = new ZipFile(file)) {
                        ZipEntry contentEntry = zipFile.getEntry(getInternalFilePath("chm"));
                        ZipEntry aliasEntry = zipFile.getEntry(getInternalFilePath("alias"));
                        if (contentEntry == null || aliasEntry == null) {
                            throw new IOException();
                        }
                        Map<String, String> files = unpackIndex(zipFile.getInputStream(aliasEntry));
                        unpackDocumentation(indicator, zipFile.getInputStream(contentEntry), documentationPath, files);
                        indicator.setText(RapidBundle.message("documentation.extract.image"));
                        images = processImages();
                    }
                    currentState = RapidDocumentationService.State.INITIALIZED;
                    showSuccessNotification(project, file, startTime);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onCancel() {
                currentState = RapidDocumentationService.State.DISABLED;
                deleteDirectory();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                NotificationGroupManager.getInstance()
                                        .getNotificationGroup("Documentation download")
                                        .createNotification(RapidBundle.message("documentation.error.title"), NotificationType.ERROR)
                                        .addAction(NotificationAction.createSimpleExpiring(RapidBundle.message("documentation.action.retry"), () -> retrieveDocumentation(project)))
                                        .notify(project);
                currentState = RapidDocumentationService.State.DISABLED;
                deleteDirectory();
                throw new RuntimeException(error);
            }

            private void deleteDirectory() {
                if (getDocumentationPath().toFile().exists()) {
                    try {
                        FileUtil.delete(getDocumentationPath());
                    } catch (IOException ignored) {}
                }
            }
        }.queue();

    }

    private void showSuccessNotification(@Nullable Project project, @NotNull File file, long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        String fileSize = StringUtil.formatFileSize(file.length());
        NotificationGroupManager.getInstance()
                                .getNotificationGroup("Documentation download")
                                .createNotification(RapidBundle.message("documentation.download.success", elapsed / 1000, elapsed % 1000, fileSize), NotificationType.INFORMATION)
                                .notify(project);
    }

    private @NotNull Map<String, Image> processImages() throws IOException {
        Map<String, Image> images = new HashMap<>();
        Path documentationPath = getDocumentationPath();
        Files.walkFileTree(documentationPath, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path path, @NotNull BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".png") || path.toString().endsWith(".gif")) {
                    Image image = ImageIO.read(path.toFile());
                    String filePath = documentationPath.relativize(path).toString();
                    images.put("http://" + filePath, image);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return Collections.unmodifiableMap(images);
    }

    private @NotNull File download() throws IOException {
        File tempDirectory = FileUtil.createTempDirectory("intellij-rapid", "documentation", true);
        DownloadableFileService service = DownloadableFileService.getInstance();
        DownloadableFileDescription fileDescription = service.createFileDescription(DOWNLOAD_PATH, DOWNLOAD_FILE);
        List<Pair<File, DownloadableFileDescription>> download = service.createDownloader(List.of(fileDescription), RapidBundle.message("documentation.download.name")).download(tempDirectory);
        List<File> files = download.stream().map(pair -> pair.getFirst()).toList();
        if (files.size() != 1) {
            throw new IOException("Expected file: " + DOWNLOAD_FILE + ", got: " + files);
        }
        return files.get(0);
    }

    private @NotNull String getInternalFilePath(@NotNull String fileExtension) {
        String languageCode = state.preferredLanguage.getCode();
        return INTERNAL_PATH + "/" + languageCode + "/" + FILE_PREFIX + "-" + languageCode.toLowerCase() + "." + fileExtension;
    }

    private void unpackDocumentation(@NotNull ProgressIndicator indicator, @NotNull InputStream packageFile, @NotNull Path directory, @NotNull Map<String, String> index) throws IOException {
        FileUtil.delete(directory);
        ChmExtractor extractor;
        try {
            extractor = new ChmExtractor(packageFile);
        } catch (TikaException e) {
            throw new IOException(e);
        }
        patchExtractor(extractor);
        List<DirectoryListingEntry> entries = extractor.getChmDirList().getDirectoryListingEntryList();
        indicator.setIndeterminate(false);
        DocumentationNodeVisitor visitor = new DocumentationNodeVisitor(index);
        for (DirectoryListingEntry entry : entries) {
            indicator.setFraction(((double) entries.indexOf(entry)) / ((double) entries.size()));
            if (entry.getName().isEmpty()) {
                continue;
            }
            String filePath = entry.getName().substring(1);
            if (!(entry.getName().startsWith("/")) || filePath.isEmpty() || filePath.startsWith("#") || filePath.startsWith("$") || filePath.endsWith(".hhk") || filePath.endsWith(".hhc")) {
                continue;
            }
            File file = getFile(directory, filePath, index);
            if (file == null) {
                continue;
            }
            if (filePath.endsWith("/")) {
                continue;
            }
            try {
                byte[] content = extractor.extractChmEntry(entry);
                writeFile(visitor, file, content);
            } catch (TikaException e) {
                throw new IOException(e);
            }
        }
    }

    private @Nullable File getFile(@NotNull Path directory, @NotNull String filePath, @NotNull Map<String, String> index) {
        File file = directory.resolve(filePath).toFile();
        if (!(filePath.endsWith("/"))) {
            String fileName = file.getName();
            if (fileName.endsWith("html")) {
                if (!(index.containsKey(fileName))) {
                    return null;
                }
                String name = index.get(fileName) + ".html";
                return file.getParentFile().toPath().resolve(name).toFile();
            }
            if (fileName.endsWith(".png")) {
                return directory.resolve(filePath.replaceAll(" ", "%20")).toFile();
            }
        }
        return file;
    }

    public void writeFile(@NotNull DocumentationNodeVisitor visitor, @NotNull File file, byte @NotNull [] content) throws IOException {
        createFile(file);
        String name = file.getName();
        String fileExtension = name.substring(name.lastIndexOf('.') + 1);
        if (fileExtension.equals("html")) {
            String text = visitor.visit(file, content);
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            writeContent(file, data);
        } else {
            writeContent(file, content);
        }
    }

    public void createFile(@NotNull File file) throws IOException {
        if (file.exists()) {
            FileUtil.delete(file);
        }
        File parentFile = file.getParentFile();
        if (parentFile.isFile()) {
            throw new IOException("Could not create parent file: " + parentFile + " since file already exists");
        }
        if (!(parentFile.exists() || parentFile.mkdirs())) {
            throw new IOException("Could not create parent file: " + parentFile);
        }
        if (!file.createNewFile()) {
            throw new IOException("Could not create file: " + file);
        }
    }

    private void writeContent(@NotNull File file, byte @NotNull [] content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(content);
        }
    }

    private @NotNull Map<String, String> unpackIndex(@NotNull InputStream inputStream) throws IOException {
        Map<String, String> index = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String entry;
            while ((entry = reader.readLine()) != null) {
                String[] sections = entry.split("\\|");
                if (sections[0].equals("\":=\"")) {
                    sections[0] = "CEQ";
                }
                if (sections[0].equals("Compact IF")) {
                    continue;
                }
                if (!(sections[0].toUpperCase().equals(sections[0]))) {
                    sections[0] = sections[0].toLowerCase();
                }
                index.put(sections[1], sections[0]);
            }
        }
        return index;
    }

    //region Temporary fix until Apache Tika is updated (https://issues.apache.org/jira/projects/TIKA/issues/TIKA-4204)
    private void patchExtractor(@NotNull ChmExtractor extractor) {
        ChmDirectoryListingSet chmDirList = extractor.getChmDirList();
        int indexOfContent = ChmCommons.indexOf(chmDirList.getDirectoryListingEntryList(), "::DataSpace/Storage/MSCompressed/Content");
        callSetter(extractor, "setIndexOfContent", indexOfContent, int.class);
        DirectoryListingEntry contentEntity = chmDirList.getDirectoryListingEntryList().get(indexOfContent);
        callSetter(extractor, "setLzxBlockOffset", contentEntity.getOffset() + chmDirList.getDataOffset(), long.class);
        callSetter(extractor, "setLzxBlockLength", contentEntity.getLength(), long.class);
    }

    private void callSetter(@NotNull ChmExtractor extractor, @NotNull String name, @NotNull Object value, @NotNull Class<?> clazz) {
        try {
            Method method = extractor.getClass().getDeclaredMethod(name, clazz);
            method.setAccessible(true);
            method.invoke(extractor, value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    public enum State {
        /**
         * Documentation has either not yet been processed - and might not be available.
         */
        UNINITIALIZED,

        /**
         * Documentation is being processed in the background.
         */
        INITIALIZING,

        /**
         * Documentation is available.
         */
        INITIALIZED,

        /**
         * Documentation is not available. An error was encountered while initializing or external documentation is
         * disabled.
         */
        DISABLED
    }
}
