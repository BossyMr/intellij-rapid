package com.bossymr.rapid.ide.execution.debugger.frame;

import com.bossymr.network.NetworkManager;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.rapid.ide.execution.debugger.RapidSourcePosition;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.QueryableSymbol;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolValue;
import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RapidStackFrame extends XStackFrame {

    private static final @NotNull Logger logger = Logger.getInstance(RapidStackFrame.class);

    private final @NotNull Project project;
    private final @NotNull StackFrame stackFrame;
    private final @Nullable String equalityObject;
    private final @Nullable RapidSourcePosition sourcePosition;
    private final @NotNull NetworkManager manager;

    public RapidStackFrame(@NotNull NetworkManager manager, @NotNull Project project, @NotNull StackFrame stackFrame) {
        this.project = project;
        this.manager = manager;
        this.equalityObject = stackFrame.getRoutine();
        this.sourcePosition = findSourcePosition(stackFrame);
        this.stackFrame = stackFrame;
    }


    private @Nullable RapidSourcePosition findSourcePosition(@NotNull StackFrame stackFrame) {
        String[] sections = stackFrame.getRoutine().split("/");
        File file = findFile(sections[1], sections[2]);
        if (file == null) return null;
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (virtualFile == null) return null;
        return ReadAction.compute(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document == null) return null;
            int offset = document.getLineStartOffset(stackFrame.getStartRow() - 1);
            return new RapidSourcePosition(virtualFile, stackFrame.getStartRow() - 1, offset);
        });
    }

    private @Nullable File findFile(@NotNull String taskName, @NotNull String moduleName) {
        RapidRobot robot = RobotService.getInstance().getRobot();
        if (robot == null) return null;
        for (RapidTask task : robot.getTasks()) {
            if (task.getName().equals(taskName)) {
                for (File file : task.getFiles()) {
                    String fileNameWithExtension = file.getName();
                    String fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
                    if (fileName.equals(moduleName)) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable Object getEqualityObject() {
        return equalityObject;
    }

    @Override
    public @Nullable RapidSourcePosition getSourcePosition() {
        return sourcePosition;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (node.isObsolete()) return;
        CompletableFuture.runAsync(() -> ReadAction.run(() -> {
            RapidSymbol symbol = ResolveService.getInstance(project).findSymbol(stackFrame.getRoutine());
            XValueChildrenList childrenList = new XValueChildrenList();
            if (!(symbol instanceof PhysicalRoutine routine)) {
                return;
            }
            for (PhysicalField field : routine.getFields()) {
                childrenList.add(new RapidSymbolValue(manager, field, stackFrame));
            }
            List<PhysicalParameterGroup> parameters = routine.getParameters();
            if (parameters != null) {
                for (PhysicalParameterGroup group : parameters) {
                    for (PhysicalParameter parameter : group.getParameters()) {
                        try {
                            QueryableSymbol queryableSymbol = RapidSymbolValue.findSymbol(manager, parameter, stackFrame);
                            SymbolValue symbolValue = queryableSymbol.getValue().get();
                            if (symbolValue.getValue().length() > 0) {
                                childrenList.add(new RapidSymbolValue(manager, parameter, stackFrame));
                            }
                        } catch (ResponseStatusException e) {
                            if (e.getResponse().code() == 400) {
                                // If an optional parameter is not provided, attempting to retrieve its value will
                                // throw a ResponseStatusException with code 400.
                                continue;
                            }
                            logger.error(e);
                        } catch (IOException | InterruptedException e) {
                            logger.error(e);
                        }
                    }
                }
            }
            PhysicalModule module = PsiTreeUtil.getStubOrPsiParentOfType(routine, PhysicalModule.class);

            if (module != null) {
                for (RapidField child : module.getFields()) {
                    childrenList.add(new RapidSymbolValue(manager, child, stackFrame));
                }
            }
            node.addChildren(childrenList, true);
        }));
    }

    @Override
    public void customizePresentation(@NotNull ColoredTextContainer component) {
        String[] sections = stackFrame.getRoutine().split("/");
        String label = sections[sections.length - 1] + ":" + stackFrame.getStartRow() + ", " + sections[2];
        component.append(label, SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidStackFrame that = (RapidStackFrame) o;
        return stackFrame.equals(that.stackFrame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stackFrame);
    }

    @Override
    public String toString() {
        return "RapidStackFrame{" +
                "stackFrame=" + stackFrame +
                '}';
    }
}
