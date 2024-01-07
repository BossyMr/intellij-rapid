package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.BlockType;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.execution.util.ExecUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public class DataFlowGraphService extends AnAction {

    public static void convert(@NotNull File outputFile, @NotNull Set<ControlFlowBlock> blocks) throws IOException, ExecutionException {
        String processName = SystemInfo.isUnix ? "dot" : "dot.exe";
        File processPath = PathEnvironmentVariableUtil.findInPath(processName);
        if (processPath == null) {
            throw new FileNotFoundException("Could not find GraphViz");
        }
        if (outputFile.exists()) {
            FileUtil.delete(outputFile);
        }
        String text = writeInstruction(blocks);
        String extension = FileUtil.getExtension(outputFile.getName(), "svg").toString();
        if (extension.equals("txt")) {
            FileUtil.writeToFile(outputFile, text);
            return;
        }
        File instructionFile = FileUtil.createTempFile("dataFlow", ".dot", true);
        try {
            if (outputFile.getParentFile() != null && !(outputFile.getParentFile().exists())) {
                if (!(outputFile.getParentFile().mkdirs())) {
                    throw new IOException("Could not create parent directory");
                }
            }
            FileUtil.writeToFile(instructionFile, text);
            GeneralCommandLine commandLine = new GeneralCommandLine(processPath.getAbsolutePath())
                    .withInput(instructionFile.getAbsoluteFile())
                    .withParameters("-Tsvg", "-o" + outputFile.getAbsolutePath(), instructionFile.getAbsolutePath())
                    .withRedirectErrorStream(true);
            ExecUtil.execAndGetOutput(commandLine);
        } finally {
            FileUtil.delete(instructionFile);
        }
        if (!(outputFile.exists())) {
            throw new ExecutionException(RapidBundle.message("data.flow.save.graph.failure"));
        }
    }

    private static @NotNull String writeInstruction(@NotNull Set<ControlFlowBlock> blocks) {
        List<DataFlowState> states = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph {").append("\n");
        stringBuilder.append("rankdir=LR;\n");
        for (ControlFlowBlock block : blocks) {
            Block controlFlow = block.getControlFlow();
            if (!(controlFlow instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            writeInstruction(stringBuilder, functionBlock, block, states);
        }
        for (ControlFlowBlock block : blocks) {
            DataFlowFunction function = block.getFunction();
            Map<DataFlowState, Set<DataFlowState>> usages = function.getUsages();
            for (DataFlowState callerState : usages.keySet()) {
                writeState(stringBuilder, states, callerState);
                for (DataFlowState calleeState : usages.get(callerState)) {
                    writeState(stringBuilder, states, calleeState);
                    DataFlowFunction.Result result = function.getResults().get(calleeState);
                    String resultType = getResultType(result);
                    stringBuilder.append(getDataFlowStateName(states, calleeState))
                                 .append(" -> ")
                                 .append(getDataFlowStateName(states, callerState))
                                 .append("[name=").append(resultType)
                                 .append("];\n");
                }
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    @NotNull
    private static String getResultType(DataFlowFunction.Result result) {
        if (result instanceof DataFlowFunction.Result.Success) {
            return "success";
        } else if (result instanceof DataFlowFunction.Result.Error) {
            return "error";
        } else if (result instanceof DataFlowFunction.Result.Exit) {
            return "exit";
        } else {
            throw new AssertionError();
        }
    }

    private static void writeInstruction(@NotNull StringBuilder stringBuilder, @NotNull Block.FunctionBlock functionBlock, @NotNull ControlFlowBlock block, @NotNull List<DataFlowState> states) {
        String blockName = functionBlock.getModuleName() + ":" + functionBlock.getName();
        stringBuilder.append("subgraph ").append(getBlockClusterName(functionBlock)).append(" {").append("\n");
        stringBuilder.append("style=dotted;\nlabel=\"").append(blockName).append("\";").append("\n");
        stringBuilder.append(getEntryBlockName(functionBlock)).append("[shape=oval;label=Entry];").append("\n");
        for (BlockType value : BlockType.values()) {
            EntryInstruction entryInstruction = functionBlock.getEntryInstruction(value);
            if (entryInstruction == null) {
                continue;
            }
            for (DataFlowState state : block.getDataFlow(entryInstruction.getInstruction())) {
                writeState(stringBuilder, states, state);
                if (state.getPredecessor() == null) {
                    stringBuilder.append(getEntryBlockName(functionBlock)).append(" -> ").append(getDataFlowStateName(states, state)).append(";\n");
                }
            }
        }
        stringBuilder.append("};").append("\n");
    }

    private static @NotNull String getEntryBlockName(@NotNull Block block) {
        String blockName = block.getModuleName() + ":" + block.getName();
        return "\"" + blockName + ":" + "entry" + "\"";
    }

    private static @NotNull String getBlockClusterName(@NotNull Block block) {
        return "\"cluster_" + block.getModuleName() + "_" + block.getName() + "\"";
    }

    private static @NotNull String getDataFlowStateName(@NotNull Block functionBlock, int index) {
        String blockName = functionBlock.getModuleName() + ":" + functionBlock.getName();
        return "\"" + blockName + ":" + index + "\"";
    }

    private static @NotNull String getDataFlowStateName(@NotNull List<DataFlowState> states, @NotNull DataFlowState state) {
        int index = states.indexOf(state);
        if (index < 0) {
            throw new IllegalArgumentException("Could not calculate name for state: " + state);
        }
        Block block = state.getFunctionBlock();
        return getDataFlowStateName(block, index);
    }

    private static void writeState(@NotNull StringBuilder stringBuilder, @NotNull List<DataFlowState> states, @NotNull DataFlowState state) {
        if (states.contains(state)) {
            return;
        }
        states.add(state);
        stringBuilder.append(getDataFlowStateName(states, state));
        stringBuilder.append("[shape=plain,label=<").append("<table BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
        Instruction instruction = state.getInstruction();
        stringBuilder.append("<tr><td COLSPAN=\"2\">").append("Instruction #").append(instruction.getIndex()).append("</td></tr>\n");
        writeInstruction(stringBuilder, instruction);
        stringBuilder.append("<tr><td COLSPAN=\"2\">").append("Snapshots").append("</td></tr>\n");
        Set<Snapshot> snapshots = new HashSet<>();
        for (Map.Entry<Field, Snapshot> entry : state.getSnapshots().entrySet()) {
            writeSnapshot(stringBuilder, state, new VariableExpression(entry.getKey()), entry.getValue(), snapshots);
        }
        stringBuilder.append("<tr><td COLSPAN=\"2\">").append("Expressions").append("</td></tr>\n");
        for (Expression condition : state.getConditions()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td COLSPAN=\"2\" CELLPADDING=\"4\" align=\"left\">");
            stringBuilder.append(HtmlChunk.text(condition.accept(new ControlFlowFormatVisitor())));
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>\n");
        }
        stringBuilder.append("</table>>];\n");
        for (DataFlowState successor : state.getSuccessors()) {
            writeState(stringBuilder, states, successor);
        }
        if (state.getPredecessor() != null) {
            writeState(stringBuilder, states, state.getPredecessor());
            stringBuilder.append(getDataFlowStateName(states, state.getPredecessor()))
                         .append(" -> ")
                         .append(getDataFlowStateName(states, state))
                         .append(";\n");
        }
    }

    private static void writeSnapshot(@NotNull StringBuilder stringBuilder, @NotNull DataFlowState state, @NotNull ReferenceExpression variable, @NotNull Snapshot snapshot, @NotNull Set<Snapshot> snapshots) {
        if (!(snapshots.add(snapshot))) {
            return;
        }
        ControlFlowFormatVisitor visitor = new ControlFlowFormatVisitor();
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(variable.accept(visitor));
        if (variable instanceof VariableExpression variableValue) {
            String name = variableValue.getField().getName();
            if (name != null) {
                stringBuilder.append("[");
                stringBuilder.append(name);
                stringBuilder.append("]");
            }
        }
        stringBuilder.append("</td>");
        stringBuilder.append("<td align=\"left\">");
        stringBuilder.append(snapshot);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>\n");
        if (snapshot instanceof RecordSnapshot recordSnapshot) {
            for (Map.Entry<String, List<RecordSnapshot.Entry>> entry : recordSnapshot.getSnapshots().entrySet()) {
                for (RecordSnapshot.Entry assignment : entry.getValue()) {
                    writeSnapshot(stringBuilder, state, new ComponentExpression(assignment.snapshot().getType(), variable, entry.getKey()), assignment.snapshot(), snapshots);
                }
            }
        } else if (snapshot instanceof ArraySnapshot arraySnapshot) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            stringBuilder.append("length");
            stringBuilder.append("</td>");
            stringBuilder.append("<td align=\"left\">");
            stringBuilder.append(arraySnapshot.getLength());
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>\n");
            for (ArraySnapshot.Entry assignment : arraySnapshot.getAssignments()) {
                stringBuilder.append("<tr>");
                stringBuilder.append("<td>");
                stringBuilder.append(assignment.index().accept(visitor));
                stringBuilder.append("</td>");
                stringBuilder.append("<td align=\"left\">");
                stringBuilder.append(assignment.snapshot());
                stringBuilder.append("</td>");
                stringBuilder.append("</tr>\n");
            }
            for (ArraySnapshot.Entry assignment : arraySnapshot.getAssignments(state)) {
                writeSnapshot(stringBuilder, state, new IndexExpression(variable, assignment.index()), assignment.snapshot(), snapshots);
            }
        }
    }

    private static void writeInstruction(@NotNull StringBuilder stringBuilder, @NotNull Instruction instruction) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td COLSPAN=\"2\" CELLPADDING=\"4\">");
        stringBuilder.append(HtmlChunk.text(instruction.accept(new ControlFlowFormatVisitor())));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>\n");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        FileSaverDescriptor descriptor = new FileSaverDescriptor(RapidBundle.message("data.flow.save.graph"), "", "svg", "txt");
        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper wrapper = dialog.save(project.getName());
        if (wrapper != null) {
            Task.Backgroundable task = new Task.Backgroundable(project, RapidBundle.message("data.flow.save.graph.task")) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ControlFlowService service = ControlFlowService.getInstance();
                    Set<ControlFlowBlock> blocks = ReadAction.compute(() -> service.getDataFlow(project));
                    try {
                        convert(wrapper.getFile(), blocks);
                        NotificationGroupManager.getInstance()
                                                .getNotificationGroup("Data flow diagrams")
                                                .createNotification(RapidBundle.message("notification.group.data.flow.export.success"), wrapper.getFile().getAbsolutePath(), NotificationType.INFORMATION)
                                                .notify(project);
                    } catch (ExecutionException e) {
                        NotificationGroupManager.getInstance()
                                                .getNotificationGroup("Data flow diagrams")
                                                .createNotification(RapidBundle.message("notification.group.data.flow.export.error"), NotificationType.ERROR)
                                                .notify(project);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            };
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        }
    }
}
