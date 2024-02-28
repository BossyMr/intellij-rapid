package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.ui.UIUtil;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.microsoft.chm.ChmCommons;
import org.apache.tika.parser.microsoft.chm.ChmDirectoryListingSet;
import org.apache.tika.parser.microsoft.chm.ChmExtractor;
import org.apache.tika.parser.microsoft.chm.DirectoryListingEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public final class RapidDocumentationService {

    private static final @NotNull String DOCUMENTATION_URL = "https://robotstudiocdn.azureedge.net/distributionpackages/RobotWare/ABB.RobotWareDoc.IRC5-6.15.rspak";
    private static final @NotNull String DOCUMENTATION_FILE_NAME = "ABB.RobotWareDoc.IRC5-6.15.rspak";
    private static final @NotNull String INTERNAL_PATH = "ABB.RobotWareDoc.IRC5-6.15/Documentation/en/";
    private static final @NotNull String CONTENT_NAME = "3HAC050917_TRM_RAPID_RW_6-en.chm";
    private static final @NotNull String INDEX_NAME = "3HAC050917_TRM_RAPID_RW_6-en.alias";

    private @NotNull State state = State.UNINITIALIZED;
    private @Nullable Map<String, Image> images;

    public static @NotNull RapidDocumentationService getInstance() {
        return ApplicationManager.getApplication().getService(RapidDocumentationService.class);
    }

    public @Nullable Result getDocumentation(@NotNull RapidSymbol symbol) {
        String name = symbol.getName();
        if (name == null) {
            return null;
        }
        return getDocumentation(name.toLowerCase());
    }

    public @Nullable Result getDocumentation(@NotNull IElementType elementType) {
        String text = elementType == RapidTokenTypes.CEQ ? "CEQ" : elementType.toString();
        return getDocumentation(text.toUpperCase());
    }


    private @Nullable Result getDocumentation(@NotNull String name) {
        if (state == State.DISABLED) {
            return null;
        }
        if (state == State.UNINITIALIZED) {
            try {
                computeDocumentation();
                state = State.INITIALIZED;
            } catch (IOException e) {
                state = State.DISABLED;
                return null;
            }
        }
        Objects.requireNonNull(images);
        File file = getDocumentationPath().resolve(name + ".html").toFile();
        if (!(file.isFile())) {
            return null;
        }
        try {
            String content = Files.readString(file.toPath());
            return new Result(content, images);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Download documentation for the specified RobotWare version.
     */
    public void computeDocumentation() throws IOException {
        if (processDocumentation()) {
            return;
        }
        Path documentationPath = getDocumentationPath();
        if (documentationPath.toFile().exists()) {
            FileUtil.delete(documentationPath);
        }
        File file = downloadDocumentation();
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry contentEntry = zipFile.getEntry(INTERNAL_PATH + CONTENT_NAME);
            ZipEntry aliasEntry = zipFile.getEntry(INTERNAL_PATH + INDEX_NAME);
            if (contentEntry == null || aliasEntry == null) {
                throw new IOException("Unexpected file content");
            }
            Map<String, String> files = unpackIndex(zipFile.getInputStream(aliasEntry));
            unpackDocumentation(zipFile.getInputStream(contentEntry), documentationPath, files);
            this.images = processImages();
        }
    }

    private @NotNull Path getDocumentationPath() {
        return PathManager.getPluginsDir().resolve("Rapid").resolve("documentation");
    }

    private @NotNull Map<String, Image> processImages() throws IOException {
        Map<String, Image> images = new HashMap<>();
        Path documentationPath = getDocumentationPath();
        Files.walkFileTree(documentationPath, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path path, @NotNull BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".png") || path.toString().endsWith(".gif")) {
                    Image image = ImageIO.read(path.toFile());
                    if (path.getParent().endsWith("Graphics")) {
                        String name = path.toFile().getName();
                        int width = image.getWidth(null) / 15;
                        int height = image.getHeight(null) / 15;
                        BufferedImage rescaled = UIUtil.createImage(null, width, height, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D graphics = rescaled.createGraphics();
                        graphics.drawImage(image, 0, 0, width, height, null);
                        ImageIO.write(rescaled, name.substring(name.lastIndexOf(".")), path.toFile());
                        image = rescaled;
                    }
                    String filePath = documentationPath.relativize(path).toString();
                    images.put("http://" + filePath, image);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return images;
    }

    private boolean processDocumentation() {
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

    private @NotNull File downloadDocumentation() throws IOException {
        File tempDirectory = FileUtil.createTempDirectory("intellij-rapid", null, true);
        DownloadableFileService service = DownloadableFileService.getInstance();
        DownloadableFileDescription fileDescription = service.createFileDescription(DOCUMENTATION_URL, DOCUMENTATION_FILE_NAME);
        List<Pair<File, DownloadableFileDescription>> result = service.createDownloader(List.of(fileDescription), RapidBundle.message("documentation.download.name"))
                                                                      .download(tempDirectory);
        if (result.size() != 1) {
            throw new IOException("Unexpected state: " + result);
        }
        return result.get(0).getFirst();
    }

    private void unpackDocumentation(@NotNull InputStream packageFile, @NotNull Path directory, @NotNull Map<String, String> files) throws IOException {
        FileUtil.delete(directory);
        ChmExtractor extractor;
        try {
            extractor = new ChmExtractor(packageFile);
        } catch (TikaException e) {
            throw new IOException(e);
        }
        patchExtractor(extractor);
        List<DirectoryListingEntry> entries = extractor.getChmDirList().getDirectoryListingEntryList();
        for (DirectoryListingEntry entry : entries) {
            String name = entry.getName().substring(1);
            if (!(entry.getName().startsWith("/")) || name.isEmpty() || name.startsWith("#") || name.startsWith("$") || name.endsWith(".hhk") || name.endsWith(".hhc") || name.endsWith(".css")) {
                continue;
            }
            File file = directory.resolve(name).toFile();
            if (file.getName().endsWith(".html")) {
                if (!(files.containsKey(file.getName()))) {
                    continue;
                }
                String newName = files.get(file.getName()) + ".html";
                file = file.getParentFile().toPath().resolve(newName).toFile();
            }
            if (name.endsWith("/")) {
                if (!(file.mkdirs())) {
                    throw new IOException("Could not create directory: " + directory);
                }
            } else {
                File parentFile = file.getParentFile();
                if (!(parentFile.exists() || parentFile.mkdirs())) {
                    throw new IOException("Could not create parent directory to file: " + file);
                }
                if (!(file.createNewFile())) {
                    throw new IOException("Could not create file: " + file);
                }
                byte[] content;
                try {
                    content = extractor.extractChmEntry(entry);
                } catch (TikaException e) {
                    throw new IOException(e);
                }
                if (file.getName().endsWith(".html")) {
                    content = patchFile(content, files);
                }
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(content);
                }
            }
        }
    }

    private byte @NotNull [] patchFile(byte @NotNull [] content, @NotNull Map<String, String> files) {
        Document document = Jsoup.parse(new String(content, StandardCharsets.UTF_8));
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String currentLink = link.attr("href");
            if (files.containsKey(currentLink)) {
                link.attr("href", DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL + files.get(currentLink));
            }
        }
        Elements images = document.select("img[src]");
        for (Element image : images) {
            String currentLink = Path.of(image.attr("src")).toString();
            image.attr("src", "http://" + currentLink);
            if (image.hasAttr("style")) {
                image.removeAttr("style");
            }
        }
        return document.toString().getBytes(StandardCharsets.UTF_8);
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
                    sections[1] = "CIF";
                }
                if (!(sections[0].toUpperCase().equals(sections[0]))) {
                    sections[0] = sections[0].toLowerCase();
                }
                index.put(sections[1], sections[0]);
            }
        }
        return index;
    }

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

    private enum State {
        UNINITIALIZED,
        INITIALIZED,
        DISABLED
    }

    public record Result(@NotNull String content, @NotNull Map<String, Image> images) {}
}
