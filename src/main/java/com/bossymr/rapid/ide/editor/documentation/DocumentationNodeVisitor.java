package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.codeInsight.documentation.DocumentationManagerUtil;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentationNodeVisitor implements NodeVisitor {

    private final @NotNull Map<String, String> files;
    private final @NotNull Set<String> symbols;

    private String currentSymbol;

    public DocumentationNodeVisitor(@NotNull Map<String, String> files) {
        this.files = files;
        this.symbols = Set.copyOf(files.values());
    }

    public @NotNull String visit(@NotNull File file, byte @NotNull [] content) {
        String fileName = file.getName();
        this.currentSymbol = fileName.substring(0, fileName.lastIndexOf('.'));
        try {
            Document document = Jsoup.parse(new String(content, StandardCharsets.UTF_8));
            document.traverse(this);
            return createTable(document);
        } finally {
            this.currentSymbol = null;
        }
    }

    @Override
    public void head(@NotNull Node node, int level) {
        if (!(node instanceof Element element)) {
            return;
        }
        switch (element.tagName()) {
            case "div" -> {
                switch (element.attr("class")) {
                    case "computerscripts" -> createCodeBlock(element, true);
                    case "titled-block-title" -> element.html("<strong>" + element.html() + "</strong>");
                }
            }
            case "span" -> {
                switch (element.attr("class")) {
                    case "script-text" -> createCodeBlock(element, false);
                    case "titled-block-title" -> element.html("<strong>" + element.html() + "</strong>");
                }
            }
            case "p" -> {
                Set<String> combinations = Set.of("tip", "note", "caution", "warning", "danger");
                if (combinations.contains(element.className())) {
                    element.html("<strong>" + element.html() + "</strong>");
                }
            }
            case "a" -> {
                if (element.hasAttr("name")) {
                    element.attr("id", element.attr("name"));
                    element.removeAttr("name");
                }
                if (!element.hasAttr("href")) {
                    break;
                }
                String currentLink = element.attr("href");
                if (currentLink.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)) {
                    break;
                }
                String symbol = currentLink;
                String fragment = null;
                if (currentLink.contains("#")) {
                    symbol = currentLink.substring(0, currentLink.indexOf('#'));
                    fragment = currentLink.substring(currentLink.indexOf('#') + 1);
                }
                if (files.containsKey(symbol)) {
                    String newLink = DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL + files.get(symbol);
                    if (fragment != null) {
                        newLink += DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL_REF_SEPARATOR + fragment;
                    }
                    element.attr("href", newLink);
                } else {
                    element.removeAttr("href");
                    element.tagName("span");
                }
            }
            case "img" -> {
                String currentLink = element.attr("src");
                element.attr("src", "http://" + Path.of(currentLink));
            }
        }
    }

    private @NotNull String createTable(@NotNull Document document) {
        List<Element> tables = document.select("body > table").stream()
                                       .filter(node -> node.className().isEmpty())
                                       .toList();
        if (tables.isEmpty()) {
            return "";
        }
        Element element = tables.get(0);
        StringBuilder buffer = new StringBuilder();
        Element content = element.selectFirst(".block-firstbodycol");
        if (content != null) {
            buffer.append(DocumentationMarkup.CONTENT_START);
            Elements elements = content.children();
            if (elements.size() == 1 && elements.get(0).tagName().equals("p")) {
                elements.get(0).unwrap();
            }
            buffer.append(content.html());
            buffer.append(DocumentationMarkup.CONTENT_END);
        }
        buffer.append(DocumentationMarkup.SECTIONS_START);
        for (Element row : element.select("tr")) {
            if (row.selectFirst(".block-firstbodycol") != null) {
                continue;
            }
            Element label = row.selectFirst(".block-labelcol");
            Element value = row.selectFirst(".block-bodycol");
            if (label == null || value == null) {
                continue;
            }
            buffer.append(DocumentationMarkup.SECTION_HEADER_START);
            buffer.append(label.html());
            buffer.append(DocumentationMarkup.SECTION_SEPARATOR);
            buffer.append(value.html());
            buffer.append(DocumentationMarkup.SECTION_END);
        }
        buffer.append(DocumentationMarkup.SECTIONS_END);
        return buffer.toString();
    }

    private void createCodeBlock(@NotNull Element element, boolean isCodeBlock) {
        StringBuilder buffer = new StringBuilder();
        element.traverse((node, level) -> {
            if (node instanceof Element elementNode && elementNode.tagName().equals("p")) {
                buffer.append("\n");
            }
            if (!(node instanceof TextNode textNode)) {
                return;
            }
            String text = textNode.getWholeText();
            if (text.isEmpty()) {
                return;
            }
            if (buffer.isEmpty() || buffer.charAt(buffer.length() - 1) == '\n' || text.contains("\n") || text.contains("\r")) {
                String previousText = text;
                text = text.lines()
                           .map(line -> {
                               if (!buffer.isEmpty() && buffer.charAt(buffer.length() - 1) != '\n' && previousText.startsWith(line)) {
                                   return line;
                               }
                               return "\t".repeat(Math.max(level - 3, 0)) + line;
                           })
                           .collect(Collectors.joining("\n"));
            }
            if (!createLink(buffer, text, isCodeBlock)) {
                if (isCodeBlock) {
                    text = highlightText(text);
                }
                TextAttributes codeAttributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(HighlighterColors.TEXT).clone();
                codeAttributes.setBackgroundColor(null);
                HtmlSyntaxInfoUtil.appendStyledSpan(buffer, codeAttributes, text, 1);
            }
        });
        element.html((isCodeBlock ? "<pre>" : "") + "<code style='font-size:90%;'>" + buffer + "</code>" + (isCodeBlock ? "</pre>" : ""));
    }

    private @NotNull String highlightText(@NotNull String text) {
        StringBuilder stringBuilder = new StringBuilder();
        Project project = ProjectManager.getInstance().getDefaultProject();
        ReadAction.run(() -> HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(stringBuilder, project, RapidLanguage.getInstance(), text, false, 1));
        return stringBuilder.toString();
    }

    private boolean createLink(@NotNull StringBuilder buffer, @NotNull String text, boolean isCodeBlock) {
        if (isCodeBlock) {
            return false;
        }
        if (text.equalsIgnoreCase(currentSymbol)) {
            return false;
        }
        if (symbols.contains(text.toLowerCase())) {
            DocumentationManagerUtil.createHyperlink(buffer, text.toLowerCase(), text, false, false);
            return true;
        }
        return false;
    }

}
