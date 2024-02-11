package com.bossymr.rapid.ide.execution.filter;

import com.bossymr.rapid.ide.execution.RapidProcessHandler;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.language.psi.stubs.index.RapidModuleIndex;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RapidFileFilter implements Filter {

    public static final Pattern FILE_PATTERN = Pattern.compile("[\\p{L}0-9]+:[0-9]+:[0-9]+");

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
                                                  if (result.group().isEmpty()) {
                                                      return null;
                                                  }
                                                  String[] sections = result.group().split(":");
                                                  String moduleName = sections[0];
                                                  try {
                                                      int row = Integer.parseInt(sections[1]) - 1;
                                                      int column = Integer.parseInt(sections[2]);
                                                      HyperlinkInfo hyperLink = getHyperLink(moduleName, row, column);
                                                      return new ResultItem(textOffset + result.start(), textOffset + result.end(), hyperLink);
                                                  } catch (NumberFormatException e) {
                                                      return null;
                                                  }
                                              })
                                              .filter(Objects::nonNull)
                                              .toList();
        return resultItems.isEmpty() ? null : new Result(resultItems);
    }

    private @Nullable HyperlinkInfo getHyperLink(@NotNull String moduleName, int row, int column) {
        RapidProcessHandler processHandler = getProcessHandler();
        if (processHandler == null) {
            return null;
        }
        RobotService robotService = RobotService.getInstance();
        RapidRobot robot = robotService.getRobot();
        if (robot == null) {
            return null;
        }
        for (TaskState taskState : processHandler.getTasks()) {
            String taskName = taskState.getName();
            if (taskName == null) {
                continue;
            }
            PhysicalModule module;
            if (taskState.getModuleName() == null) {
                RapidTask task = robot.getTask(taskName);
                if (task == null) {
                    continue;
                }
                Optional<PhysicalModule> result = task.getModules(project).stream()
                                                      .filter(remoteModule -> moduleName.equalsIgnoreCase(remoteModule.getName()))
                                                      .findFirst();
                if (result.isEmpty()) {
                    continue;
                }
                module = result.orElseThrow();
            } else {
                Collection<PhysicalModule> modules = RapidModuleIndex.getInstance().getElements(moduleName, project, GlobalSearchScope.projectScope(project));
                if (modules.isEmpty()) {
                    continue;
                }
                module = modules.iterator().next();
            }
            return project -> ApplicationManager.getApplication().invokeLater(() -> {
                PsiFile containingFile = module.getContainingFile();
                Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
                if (document == null) {
                    return;
                }
                VirtualFile virtualFile = containingFile.getVirtualFile();
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, document.getLineStartOffset(row) + column);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            });
        }
        return null;
    }

    private @Nullable RapidProcessHandler getProcessHandler() {
        ProcessHandler[] processes = ExecutionManager.getInstance(project).getRunningProcesses();
        for (ProcessHandler process : processes) {
            if (process instanceof RapidProcessHandler processHandler) {
                return processHandler;
            }
        }
        return null;
    }
}
