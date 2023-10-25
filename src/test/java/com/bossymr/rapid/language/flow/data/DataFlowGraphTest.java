package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataFlowGraphTest extends BasePlatformTestCase {

    private static final int MAX_PASSES = 5;

    private void check(@NotNull String text) throws IOException, ExecutionException {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        File outputDirectory = Path.of(System.getProperty("user.home"), "graph", getTestName(false)).toFile();
        if (outputDirectory.exists()) {
            FileUtil.delete(outputDirectory);
        }
        if (!(outputDirectory.exists() || outputDirectory.mkdir())) {
            throw new IOException("Could not create output folder");
        }
        Map<Instruction, AtomicInteger> passes = new HashMap<>();
        ControlFlow controlFlow = ControlFlowService.getInstance().getControlFlow(getProject());
        String output = ControlFlowFormatVisitor.format(controlFlow);
        Path path = outputDirectory.toPath();
        FileUtil.writeToFile(path.resolve("flow.txt").toFile(), output);
        AtomicInteger total = new AtomicInteger();
        DataFlow result = ControlFlowService.getInstance().getDataFlow(controlFlow, (dataFlow, block) -> {
            Instruction instruction = block.getInstruction();
            Block functionBlock = instruction.getBlock();
            passes.computeIfAbsent(instruction, key -> new AtomicInteger());
            int pass = passes.get(instruction).getAndIncrement();
            File outputFile = path.resolve(total.getAndIncrement() + " " + functionBlock.getModuleName() + "-" + functionBlock.getName() + " Pass #" + pass + " Block #" + block.getInstruction().getIndex() + ".svg").toFile();
            try {
                DataFlowGraphService.convert(outputFile, dataFlow);
            } catch (IOException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            return pass <= MAX_PASSES;
        });
        File outputFile = path.resolve("complete.svg").toFile();
        DataFlowGraphService.convert(outputFile, result);
    }

    public void testModule() throws IOException, ExecutionException {
        check("""
                MODULE foo
                    PROC bar(\\num x)
                        VAR num y := 5;
                        VAR num z := 0;
                        IF Present(x) THEN
                            y := y + x;
                        ELSE
                            y := y - x;
                            z := x;
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }
}
