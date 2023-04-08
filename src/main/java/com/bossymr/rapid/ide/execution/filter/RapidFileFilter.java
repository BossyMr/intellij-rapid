package com.bossymr.rapid.ide.execution.filter;

import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RapidFileFilter implements Filter {

    // a-zA-Z0-9À-ÖØ-öø-ÿ
    public static final Pattern FILE_PATTERN = Pattern.compile("(/[\\p{L}0-9]+)+");

    private final @NotNull Project project;

    public RapidFileFilter(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @Nullable Result applyFilter(@NotNull String line, int entireLength) {
        int textOffset = entireLength - line.length();
        Matcher matcher = FILE_PATTERN.matcher(line);
        List<ResultItem> resultItems = matcher.results()
                .map(result -> {
                    if(result.group().isEmpty()) {
                        return null;
                    }
                    String[] sections = result.group().substring(1).split("/");
                    if(sections.length != 4) {
                        return null;
                    }
                    HyperlinkInfo hyperLink = getHyperLink(sections);
                    return new ResultItem(textOffset + result.start(), textOffset + result.end(), hyperLink);
                })
                .filter(Objects::nonNull)
                .toList();
        return resultItems.isEmpty() ? null : new Result(resultItems);
    }

    private @Nullable HyperlinkInfo getHyperLink(@NotNull String[] sections) {
        RemoteRobotService robotService = RemoteRobotService.getInstance();
        RapidRobot robot = robotService.getRobot().getNow(null);
        if(robot == null) {
            return null;
        }
        for (RapidTask task : robot.getTasks()) {
            for (PhysicalModule module : task.getModules(project)) {
                if(sections[0].equalsIgnoreCase(module.getName())) {
                    return project -> ApplicationManager.getApplication().invokeLater(() -> {
                        PsiFile containingFile = module.getContainingFile();
                        Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
                        if(document == null) {
                            return;
                        }
                        int line = Integer.parseInt(sections[3]) - 1;
                        TextRange textRange = new TextRange(document.getLineStartOffset(line), document.getLineEndOffset(line));
                        String text = document.getText(textRange);
                        int indexOf = text.toLowerCase().indexOf(sections[2].toLowerCase());
                        if(indexOf < 0) {
                            return;
                        }
                        VirtualFile virtualFile = containingFile.getVirtualFile();
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, document.getLineStartOffset(line) + indexOf);
                        FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
                    });
                }
            }
        }
        return null;
    }
}
