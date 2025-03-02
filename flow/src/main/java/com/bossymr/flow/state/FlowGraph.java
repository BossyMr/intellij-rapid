package com.bossymr.flow.state;

import com.bossymr.flow.Instruction;
import com.bossymr.flow.expression.Expression;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FlowGraph {

    private FlowGraph() {}

    public static String getText(List<FlowMethod> methods) {
        StringBuilder builder = new StringBuilder();
        List<FlowSnapshot> snapshots = new ArrayList<>();
        builder.append("digraph {\n");
        builder.append("rankdir=LR;\n");
        for (FlowMethod method : methods) {
            writeMethod(builder, snapshots, method);
        }
        builder.append("}");
        return builder.toString();
    }

    public static void getGraph(File outputFile, List<FlowMethod> methods) throws IOException, InterruptedException {
        if (outputFile.getParentFile() != null) {
            Files.createDirectories(outputFile.getParentFile().toPath());
        }
        String text = getText(methods);
        Path instructionFile = Files.createTempFile("dataFlow", ".dot");
        Files.writeString(instructionFile, text);
        Process process = new ProcessBuilder("dot", "-Tsvg", "\"" + instructionFile.toAbsolutePath() + "\"")
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(outputFile)
                .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String message = new String(process.getErrorStream().readAllBytes(), Charset.defaultCharset());
            throw new IOException("process exited with code: " + exitCode + " - " + message);
        }
    }

    private static void writeMethod(StringBuilder builder, List<FlowSnapshot> snapshots, FlowMethod method) {
        builder.append("subgraph ");
        builder.append(method.getMethod().getName());
        builder.append(" {\nstyle=dotted;\n");
        Deque<FlowSnapshot> queue = new ArrayDeque<>();
        queue.add(method.getEntryPoint());
        while (!queue.isEmpty()) {
            FlowSnapshot snapshot = queue.pop();
            queue.addAll(snapshot.getSuccessors());
            writeSnapshot(builder, snapshots, snapshot);
            FlowSnapshot predecessor = snapshot.getPredecessor();
            if (predecessor != null) {
                builder.append(snapshots.indexOf(predecessor));
                builder.append(" -> ");
                builder.append(snapshots.size() - 1);
                builder.append(";\n");
            }
        }
        builder.append("}\n");
    }

    private static void writeSnapshot(StringBuilder builder, List<FlowSnapshot> snapshots, FlowSnapshot snapshot) {
        snapshots.add(snapshot);
        builder.append(snapshots.size() - 1);
        builder.append("[shape=plain,label=<<table BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\">");
        Instruction instruction = snapshot.getInstruction();
        if (instruction != null) {
            builder.append("<tr><td>").append(writeText(instruction.toString())).append("</td></tr>\n");
        }
        builder.append("<tr><td>").append("Constraints").append("</td></tr>\n");
        for (Expression constraint : snapshot.getConstraints()) {
            builder.append("<tr><td>").append(writeText(constraint.toString())).append("</td></tr>\n");
        }
        builder.append("<tr><td>").append("Stack").append("</td></tr>\n");
        for (Expression expression : snapshot.getStack().reversed()) {
            builder.append("<tr><td>").append(writeText(expression.toString())).append("</td></tr>\n");
        }
        builder.append("</table>>];\n");
    }

    private static String writeText(String text) {
        return text.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }
}
