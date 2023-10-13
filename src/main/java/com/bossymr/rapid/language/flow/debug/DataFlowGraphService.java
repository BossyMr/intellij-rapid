package com.bossymr.rapid.language.flow.debug;

import com.bossymr.network.MultiMap;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowEdge;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.StatementListType;
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

    public static void convert(@NotNull File outputFile, @NotNull DataFlow dataFlow) throws IOException, ExecutionException {
        String processName = SystemInfo.isUnix ? "dot" : "dot.exe";
        File processPath = PathEnvironmentVariableUtil.findInPath(processName);
        if (processPath == null) {
            throw new FileNotFoundException("Could not find GraphViz");
        }
        if (outputFile.exists()) {
            FileUtil.delete(outputFile);
        }
        String text = writeInstruction(dataFlow);
        String extension = FileUtil.getExtension(outputFile.getName(), "svg").toString();
        if (extension.equals("txt")) {
            FileUtil.writeToFile(outputFile, text);
            return;
        }
        File instructionFile = FileUtil.createTempFile("data-flow", ".dot", true);
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

    private static @NotNull MultiMap<DataFlowBlock, DataFlowState> getStates(@NotNull DataFlow dataFlow) {
        MultiMap<DataFlowBlock, DataFlowState> states = new MultiMap<>(HashSet::new);
        Set<DataFlowBlock> visited = new HashSet<>();
        for (DataFlowBlock block : dataFlow.getBlocks()) {
            getStates(states, visited, block);
        }
        return new MultiMap<>(states);
    }

    private static void getStates(@NotNull MultiMap<DataFlowBlock, DataFlowState> states, @NotNull Set<DataFlowBlock> visited, @NotNull DataFlowBlock block) {
        if (!(visited.add(block))) {
            return;
        }
        for (DataFlowState state : block.getStates()) {
            states.put(block, state);
            if (state.getPredecessor().isPresent()) {
                DataFlowState predecessor = state.getPredecessor().orElseThrow();
                if (predecessor.getBlock().isPresent()) {
                    states.put(predecessor.getBlock().orElseThrow(), predecessor);
                    getStates(states, visited, predecessor.getBlock().orElseThrow());
                }
            }
        }
        for (DataFlowEdge successor : block.getSuccessors()) {
            getStates(states, visited, successor.getDestination());
        }
        for (DataFlowEdge predecessor : block.getPredecessors()) {
            getStates(states, visited, predecessor.getSource());
        }
    }

    private static @NotNull String writeInstruction(@NotNull DataFlow dataFlow) {
        MultiMap<DataFlowBlock, DataFlowState> states = getStates(dataFlow);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph {").append("\n");
        stringBuilder.append("compound=true;\n");
        stringBuilder.append("rankdir=LR;\n");
        ControlFlow controlFlow = dataFlow.getControlFlow();
        for (Block block : controlFlow.getBlocks()) {
            if (!(block instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            writeBlock(stringBuilder, functionBlock, dataFlow, states);
        }
        for (DataFlowBlock block : dataFlow.getUsages().keySet()) {
            DataFlowUsage usage = dataFlow.getUsages().get(block);
            for (DataFlowBlock callerBlock : usage.usages()) {
                stringBuilder.append(getStateIndex(block, 0)).append(" -> ").append(getStateIndex(callerBlock, 0));
                stringBuilder.append("[label=\"");
                stringBuilder.append(switch (usage.usageType()) {
                    case SUCCESS -> "success";
                    case ERROR -> "error";
                    case EXIT -> "exit";
                });
                stringBuilder.append("\" ltail=").append(getBasicBlockClusterName(block.getBasicBlock())).append(" lhead=").append(getBasicBlockClusterName(callerBlock.getBasicBlock()));
                stringBuilder.append("]\n");
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static void writeBlock(@NotNull StringBuilder stringBuilder, @NotNull Block.FunctionBlock block, @NotNull DataFlow dataFlow, @NotNull MultiMap<DataFlowBlock, DataFlowState> states) {
        String blockName = block.getModuleName() + ":" + block.getName();
        stringBuilder.append("subgraph ").append(getBlockClusterName(block)).append(" {").append("\n");
        stringBuilder.append("style=dotted;\nlabel=\"").append(blockName).append("\";").append("\n");
        stringBuilder.append(getEntryBlockIndex(block)).append("[shape=oval;label=Entry];").append("\n");
        for (StatementListType value : StatementListType.values()) {
            BasicBlock entryBlock = block.getEntryInstruction(value);
            if (entryBlock == null) {
                continue;
            }
            DataFlowBlock entryFlowBlock = dataFlow.getBlock(entryBlock);
            if (entryFlowBlock != null) {
                for (DataFlowState state : entryFlowBlock.getStates()) {
                    stringBuilder.append(getEntryBlockIndex(block)).append(" -> ").append(getStateIndex(entryFlowBlock, states, state)).append(";").append("\n");
                }
            }
        }
        for (BasicBlock basicBlock : block.getInstructions()) {
            DataFlowBlock dataFlowBlock = dataFlow.getBlock(basicBlock);
            if (dataFlowBlock == null) {
                continue;
            }
            stringBuilder.append("subgraph ").append(getBasicBlockClusterName(basicBlock)).append(" {\n");
            stringBuilder.append("label=<");
            stringBuilder.append("<table BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
            stringBuilder.append("<tr><td COLSPAN=\"3\">").append("Block #").append(basicBlock.getIndex()).append("</td></tr>\n");
            int index = 0;
            for (LinearInstruction instruction : basicBlock.getInstructions()) {
                writeInstruction(stringBuilder, index, instruction);
                index += 1;
            }
            writeInstruction(stringBuilder, index, basicBlock.getTerminator());
            stringBuilder.append("</table>>;");
            Collection<DataFlowState> currentStates = states.getAll(dataFlowBlock);
            for (DataFlowState state : currentStates) {
                writeState(stringBuilder, states, dataFlowBlock, state);
            }
            stringBuilder.append("};\n");
            if (currentStates.isEmpty()) {
                for (DataFlowEdge predecessor : dataFlowBlock.getPredecessors()) {
                    DataFlowBlock source = predecessor.getSource();
                    stringBuilder.append(getStateIndex(source, 0)).append(" -> ").append(getStateIndex(dataFlowBlock, 0));
                    stringBuilder.append("[ltail=").append(getBasicBlockClusterName(source.getBasicBlock())).append(" lhead=").append(getBasicBlockClusterName(basicBlock));
                    stringBuilder.append("]\n");
                }
            }
            for (DataFlowState state : states.getAll(dataFlowBlock)) {
                writePredecessor(stringBuilder, states, state, dataFlowBlock);
            }
        }
        stringBuilder.append("};").append("\n");
    }

    private static void writePredecessor(@NotNull StringBuilder stringBuilder, @NotNull MultiMap<DataFlowBlock, DataFlowState> states, @NotNull DataFlowState state, @NotNull DataFlowBlock block) {
        if (state.getPredecessor().isPresent()) {
            DataFlowState predecessor = state.getPredecessor().orElseThrow();
            Optional<DataFlowBlock> predecessorBlock = predecessor.getBlock();
            if (predecessorBlock.isEmpty()) {
                return;
            }
            if (states.getAll(predecessorBlock.orElseThrow()).contains(predecessor)) {
                stringBuilder.append(getStateIndex(predecessorBlock.orElseThrow(), states, predecessor));
                stringBuilder.append(" -> ");
                stringBuilder.append(getStateIndex(block, states, state));
                stringBuilder.append(";\n");
            } else {
                stringBuilder.append(getStateIndex(predecessorBlock.orElseThrow(), 0)).append(" -> ").append(getStateIndex(block, states, state));
                stringBuilder.append("[ltail=").append(getBasicBlockClusterName(block.getBasicBlock())).append(" lhead=").append(getBasicBlockClusterName(block.getBasicBlock()));
                stringBuilder.append("]\n");
            }
        }
    }

    private static @NotNull String getBlockClusterName(@NotNull Block block) {
        return "\"cluster_" + block.getModuleName() + "_" + block.getName() + "\"";
    }

    private static @NotNull String getBasicBlockClusterName(@NotNull BasicBlock basicBlock) {
        String clusterName = getBlockClusterName(basicBlock.getBlock());
        int index = basicBlock.getIndex();
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        return clusterName.substring(0, clusterName.length() - 1) + "_" + index + "\"";
    }

    private static void writeState(@NotNull StringBuilder stringBuilder, @NotNull MultiMap<DataFlowBlock, DataFlowState> states, @NotNull DataFlowBlock block, @NotNull DataFlowState state) {
        stringBuilder.append(getStateIndex(block, states, state));
        stringBuilder.append("[shape=plain,label=<").append("<table BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
        stringBuilder.append("<tr><td COLSPAN=\"2\">").append("State #").append(new ArrayList<>(states.getAll(block)).indexOf(state)).append("</td></tr>\n");
        stringBuilder.append("<tr><td COLSPAN=\"2\">").append("Snapshots").append("</td></tr>\n");
        Set<SnapshotExpression> snapshots = new HashSet<>();
        for (var entry : state.getSnapshots().entrySet()) {
            writeSnapshot(stringBuilder, state, new VariableExpression(entry.getKey()), entry.getValue(), snapshots);
        }
        stringBuilder.append("<tr><td COLSPAN=\"2\">").append("Expressions").append("</td></tr>\n");
        for (Expression condition : state.getExpressions()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td COLSPAN=\"2\" align=\"left\">");
            stringBuilder.append(HtmlChunk.text(condition.accept(new ControlFlowFormatVisitor())));
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>\n");
        }
        if(!(state.getOptionality().isEmpty())) {
            stringBuilder.append("<tr><td COLSPAN=\"2\">").append("Optionality").append("</td></tr>\n");
            for (var entry : state.getOptionality().entrySet()) {
                stringBuilder.append("<tr>");
                stringBuilder.append("<td>");
                stringBuilder.append(entry.getKey().accept(new ControlFlowFormatVisitor()));
                stringBuilder.append("</td>");
                stringBuilder.append("<td align=\"left\">");
                stringBuilder.append(switch (entry.getValue()) {
                    case PRESENT -> "[present]";
                    case UNKNOWN -> "[present, missing]";
                    case MISSING -> "[missing]";
                    case NO_VALUE, ANY_VALUE -> "[]";
                });
                stringBuilder.append("</td>");
                stringBuilder.append("</tr>\n");
            }
        }
        stringBuilder.append("</table>>];\n");
    }

    private static void writeSnapshot(@NotNull StringBuilder stringBuilder, @NotNull DataFlowState state, @NotNull ReferenceExpression variable, @NotNull SnapshotExpression snapshot, @NotNull Set<SnapshotExpression> snapshots) {
        if(!(snapshots.add(snapshot))) {
            return;
        }
        ControlFlowFormatVisitor visitor = new ControlFlowFormatVisitor();
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(variable.accept(visitor));
        if(variable instanceof VariableExpression variableValue && variableValue.getField().getName() != null) {
            stringBuilder.append("[");
            stringBuilder.append(variableValue.getField().getName());
            stringBuilder.append("]");
        }
        stringBuilder.append("</td>");
        stringBuilder.append("<td align=\"left\">");
        stringBuilder.append(snapshot.accept(visitor));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>\n");
        if (snapshot instanceof RecordSnapshot recordSnapshot) {
            for (var component : recordSnapshot.getSnapshots().entrySet()) {
                stringBuilder.append("<tr>");
                stringBuilder.append("<td></td>");
                stringBuilder.append("<td>");
                stringBuilder.append(component.getKey());
                stringBuilder.append("</td>");
                stringBuilder.append("<td align=\"left\">");
                stringBuilder.append(component.getValue().accept(visitor));
                stringBuilder.append("</td>");
                stringBuilder.append("</tr>\n");
            }
            for (var entry : recordSnapshot.getSnapshots().entrySet()) {
                if (entry.getValue() instanceof SnapshotExpression componentSnapshot) {
                    writeSnapshot(stringBuilder, state, new ComponentExpression(componentSnapshot.getType(), recordSnapshot, entry.getKey()), componentSnapshot, snapshots);
                }
            }
        } else if (snapshot instanceof ArraySnapshot arraySnapshot) {
            for (ArrayEntry assignmentEntry : arraySnapshot.getAssignments()) {
                if (assignmentEntry instanceof ArrayEntry.Assignment assignment) {
                    stringBuilder.append("<tr>");
                    stringBuilder.append("<td>");
                    stringBuilder.append(assignment.index().accept(visitor));
                    stringBuilder.append("</td>");
                    stringBuilder.append("<td align=\"left\">");
                    stringBuilder.append(assignment.value().accept(visitor));
                    stringBuilder.append("</td>");
                    stringBuilder.append("</tr>\n");
                } else if (assignmentEntry instanceof ArrayEntry.DefaultValue defaultValue) {
                    stringBuilder.append("<tr>");
                    stringBuilder.append("[default]");
                    stringBuilder.append("</td>");
                    stringBuilder.append("<td align=\"left\">");
                    stringBuilder.append(defaultValue.defaultValue().accept(visitor));
                    stringBuilder.append("</td>");
                    stringBuilder.append("</tr>\n");
                }
            }
            for (ArrayEntry assignmentEntry : arraySnapshot.getAllAssignments(state)) {
                if (assignmentEntry instanceof ArrayEntry.Assignment assignment) {
                    if (assignment.value() instanceof SnapshotExpression referenceValue) {
                        writeSnapshot(stringBuilder, state, new IndexExpression(arraySnapshot, assignment.index()), referenceValue, snapshots);
                    }
                } else if (assignmentEntry instanceof ArrayEntry.DefaultValue defaultValue) {
                    if (defaultValue.defaultValue() instanceof SnapshotExpression referenceValue) {
                        writeSnapshot(stringBuilder, state, new IndexExpression(arraySnapshot, new ConstantExpression("[default]")), referenceValue, snapshots);
                    }
                }
            }
        }
    }

    private static void writeInstruction(@NotNull StringBuilder stringBuilder, int index, @NotNull Instruction instruction) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>").append(index).append("</td>");
        stringBuilder.append("<td align=\"left\" CELLPADDING=\"4\">");
        stringBuilder.append(HtmlChunk.text(instruction.accept(new ControlFlowFormatVisitor())));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>\n");
    }

    private static @NotNull String getEntryBlockIndex(@NotNull Block block) {
        String blockName = block.getModuleName() + ":" + block.getName();
        return "\"" + blockName + ":" + "entry" + "\"";
    }

    private static @NotNull String getStateIndex(@NotNull DataFlowBlock block, int index) {
        BasicBlock basicBlock = block.getBasicBlock();
        Block functionBlock = basicBlock.getBlock();
        String blockName = functionBlock.getModuleName() + ":" + functionBlock.getName();
        String stateName = blockName + ":" + basicBlock.getIndex() + ":" + index;
        return "\"" + stateName + "\"";
    }

    private static @NotNull String getStateIndex(@NotNull DataFlowBlock block, @NotNull MultiMap<DataFlowBlock, DataFlowState> states, @NotNull DataFlowState state) {
        int index = new ArrayList<>(states.getAll(block)).indexOf(state);
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        return getStateIndex(block, index);
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
                    DataFlow dataFlow = ReadAction.compute(() -> service.getDataFlow(project));
                    try {
                        convert(wrapper.getFile(), dataFlow);
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
