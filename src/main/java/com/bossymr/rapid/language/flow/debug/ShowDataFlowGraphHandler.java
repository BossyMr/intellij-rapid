package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.DataFlowEdge;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.StatementListType;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.execution.util.ExecUtil;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ShowDataFlowGraphHandler implements LanguageCodeInsightActionHandler {

    public static void convert(@NotNull File outputFile, @NotNull DataFlow dataFlow) throws IOException, ExecutionException {
        String processName = SystemInfo.isUnix ? "dot" : "dot.exe";
        File processPath = PathEnvironmentVariableUtil.findInPath(processName);
        if (processPath == null) {
            throw new FileNotFoundException("Could not find GraphViz");
        }
        File instructionFile = FileUtil.createTempFile("data-flow", ".dot", true);
        try {
            String text = writeInstruction(dataFlow);
            FileUtil.writeToFile(instructionFile, text);
            GeneralCommandLine commandLine = new GeneralCommandLine(processPath.getAbsolutePath())
                    .withInput(instructionFile.getAbsoluteFile())
                    .withParameters("-Tsvg", "-o" + outputFile.getAbsolutePath(), instructionFile.getAbsolutePath())
                    .withRedirectErrorStream(true);
            ExecUtil.execAndGetOutput(commandLine);
        } finally {
            FileUtil.delete(instructionFile);
        }
    }

    private static @NotNull String writeInstruction(@NotNull DataFlow dataFlow) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph {").append("\n");
        stringBuilder.append("rankdir=LR;\n");
        ControlFlow controlFlow = dataFlow.getControlFlow();
        for (Block block : controlFlow.getBlocks()) {
            writeBlock(stringBuilder, block, dataFlow);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static void writeBlock(@NotNull StringBuilder stringBuilder, @NotNull Block block, @NotNull DataFlow dataFlow) {
        String blockName = block.getModuleName() + ":" + block.getName();
        stringBuilder.append("subgraph \"cluster_").append(blockName).append("\" {").append("\n");
        stringBuilder.append("style=dotted;\nlabel=\"").append(blockName).append("\";").append("\n");
        stringBuilder.append(getBlockIndex(block, "entry")).append("[shape=oval;label=Entry];").append("\n");
        stringBuilder.append(getBlockIndex(block, "exit")).append("[shape=oval;label=Exit];").append("\n");
        for (StatementListType value : StatementListType.values()) {
            BasicBlock entryBlock = block.getEntryBlock(value);
            if (entryBlock == null) {
                continue;
            }
            stringBuilder.append(getBlockIndex(block, "entry")).append(" -> ").append(getBlockIndex(entryBlock)).append(";").append("\n");
        }
        for (BasicBlock basicBlock : block.getBasicBlocks()) {
            DataFlowBlock dataFlowBlock = dataFlow.getBlock(basicBlock);
            stringBuilder.append(getBlockIndex(basicBlock));
            stringBuilder.append("[shape=plain,label=<");
            stringBuilder.append("<table BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
            stringBuilder.append("<tr><td COLSPAN=\"3\">").append("Block #").append(basicBlock.getIndex()).append("</td></tr>\n");
            int index = 0;
            for (LinearInstruction instruction : basicBlock.getInstructions()) {
                writeInstruction(stringBuilder, index, instruction);
                index += 1;
            }
            writeInstruction(stringBuilder, index, basicBlock.getTerminator());
            index = 0;
            for (DataFlowState state : dataFlowBlock.getStates()) {
                writeState(stringBuilder, index, state);
                index += 1;
            }
            stringBuilder.append("</table>");
            stringBuilder.append(">];").append("\n");
            if (dataFlowBlock.getSuccessors().isEmpty()) {
                stringBuilder.append(getBlockIndex(basicBlock)).append(" -> ").append(getBlockIndex(block, "exit")).append(";").append("\n");
            }
            for (DataFlowEdge successor : dataFlowBlock.getSuccessors()) {
                stringBuilder.append(getBlockIndex(basicBlock)).append(" -> ").append(getBlockIndex(successor.getBlock().getBasicBlock()));
                List<DataFlowState> states = successor.getBlock().getStates();
                stringBuilder.append("[label=\"");
                stringBuilder.append(successor.getStates().stream()
                        .map(state -> String.valueOf(states.indexOf(state)))
                        .collect(Collectors.joining(", ")));
                stringBuilder.append("\"]");
                stringBuilder.append(";").append("\n");
            }
        }
        stringBuilder.append("};").append("\n");
    }

    private static void writeState(@NotNull StringBuilder stringBuilder, int index, @NotNull DataFlowState state) {
        stringBuilder.append("<tr><td COLSPAN=\"3\">").append("State #").append(index).append("</td></tr>\n");
        stringBuilder.append("<tr><td COLSPAN=\"3\">").append("Snapshots").append("</td></tr>\n");
        for (var entry : state.getSnapshots().entrySet()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            entry.getKey().accept(new ControlFlowFormatVisitor(stringBuilder));
            stringBuilder.append("</td>");
            stringBuilder.append("<td COLSPAN=\"2\" align=\"left\">");
            entry.getValue().accept(new ControlFlowFormatVisitor(stringBuilder));
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>\n");
        }
        stringBuilder.append("<tr><td COLSPAN=\"3\">").append("Conditions").append("</td></tr>\n");
        for (Condition condition : state.getConditions()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            StringBuilder temporaryBuilder = new StringBuilder();
            condition.getVariable().accept(new ControlFlowFormatVisitor(temporaryBuilder));
            stringBuilder.append(HtmlChunk.text(temporaryBuilder.toString()));
            stringBuilder.append("</td>");
            stringBuilder.append("<td>");
            stringBuilder.append(HtmlChunk.text(switch (condition.getConditionType()) {
                case EQUALITY -> "=";
                case INEQUALITY -> "!=";
                case LESS_THAN -> "<";
                case LESS_THAN_OR_EQUAL -> "<=";
                case GREATER_THAN -> ">";
                case GREATER_THAN_OR_EQUAL -> ">=";
            }));
            stringBuilder.append("</td>");
            stringBuilder.append("<td align=\"left\">");
            temporaryBuilder = new StringBuilder();
            condition.getExpression().accept(new ControlFlowFormatVisitor(temporaryBuilder));
            stringBuilder.append(HtmlChunk.text(temporaryBuilder.toString()));
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>\n");
        }
        stringBuilder.append("<tr><td COLSPAN=\"3\">").append("Constraints").append("</td></tr>\n");
        for (var entry : state.getConstraints().entrySet()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            entry.getKey().accept(new ControlFlowFormatVisitor(stringBuilder));
            stringBuilder.append("</td>");
            stringBuilder.append("<td align=\"left\">");
            stringBuilder.append(entry.getValue().getPresentableText());
            stringBuilder.append("</td>");
            stringBuilder.append("<td align=\"left\">");
            stringBuilder.append(switch (entry.getValue().getOptionality()) {
                case PRESENT -> "[present]";
                case UNKNOWN -> "[present, missing]";
                case MISSING -> "[missing]";
                case NO_VALUE -> "[]";
            });
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>\n");
        }
    }

    private static void writeInstruction(@NotNull StringBuilder stringBuilder, int index, @NotNull Instruction instruction) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>").append(index).append("</td>");
        stringBuilder.append("<td COLSPAN=\"2\" align=\"left\" CELLPADDING=\"4\">");
        StringBuilder temporaryBuilder = new StringBuilder();
        instruction.accept(new ControlFlowFormatVisitor(temporaryBuilder));
        stringBuilder.append(HtmlChunk.text(temporaryBuilder.toString()));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>\n");
    }

    private static @NotNull String getBlockIndex(@NotNull Block block, @NotNull String name) {
        String blockName = block.getModuleName() + ":" + block.getName();
        return "\"" + blockName + ":" + name + "\"";
    }

    private static @NotNull String getBlockIndex(@NotNull BasicBlock basicBlock) {
        return getBlockIndex(basicBlock.getBlock(), String.valueOf(basicBlock.getIndex()));
    }

    @Override
    public boolean isValidFor(@NotNull Editor editor, @NotNull PsiFile file) {
        return file instanceof RapidFile;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        Module module = ModuleUtil.findModuleForFile(file);
        if (module != null) {
            // TODO: 2023-07-27 Let user choose download file, show notification once complete, or error
        }
    }

}
