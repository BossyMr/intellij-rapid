package io.github.bossymr.ide.highlight;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.ResourceUtil;
import io.github.bossymr.RapidBundle;
import io.github.bossymr.RapidIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class RapidColorSettingsPage implements ColorSettingsPage {

    @Override
    public @Nullable Icon getIcon() {
        return RapidIcons.RAPID;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new RapidHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        // TODO: 2022-06-23 Add additional tags to showcase additional attributes (such as "unused declaration")
        try (InputStream inputStream = getClass().getResourceAsStream("/io/github/bossymr/ide/highlight/highlightText.mod")) {
            assert inputStream != null;
            return ResourceUtil.loadText(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Arrays.stream(RapidColor.values())
                .collect(Collectors.toMap(Enum::name, color -> color.textAttributesKey));
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return Arrays.stream(RapidColor.values())
                .map(color -> color.attributesDescriptor)
                .toArray(AttributesDescriptor[]::new);
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return RapidBundle.message("settings.rapid.color.scheme.title");
    }
}
