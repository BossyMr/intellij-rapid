package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

public class DataFlowGraphTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ControlFlowService.getInstance().reload();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/com/bossymr/rapid/ide/insight/flow/";
    }

    private void checkByText(@NotNull String text) throws IOException, ExecutionException {
        check(() -> {
            myFixture.configureByText(RapidFileType.getInstance(), text);
            ControlFlowService service = ControlFlowService.getInstance();
            return service.getDataFlow(myFixture.getProject());
        });
    }

    private void checkByFile(@NotNull String fileName) throws IOException, ExecutionException {
        check(() -> {
            myFixture.configureByFile(fileName);
            ControlFlowService service = ControlFlowService.getInstance();
            return service.getDataFlow(myFixture.getProject());
        });
    }

    private void check(@NotNull Supplier<Set<ControlFlowBlock>> supplier) throws IOException, ExecutionException {
        Set<ControlFlowBlock> dataFlow = supplier.get();
        String name = getTestName(true);
        File outputDirectory = Path.of(System.getProperty("user.home"), "graph", name).toFile();
        Path path = outputDirectory.toPath();
        if (outputDirectory.exists()) {
            FileUtil.delete(outputDirectory);
        }
        if (!(outputDirectory.exists() || outputDirectory.mkdirs())) {
            throw new IOException("Could not create output folder");
        }
        String output = ControlFlowFormatVisitor.format(dataFlow);
        FileUtil.writeToFile(path.resolve("controlFlow.txt").toFile(), output);
        File outputFile = path.resolve("dataFlow.svg").toFile();
        DataFlowGraphService.convert(outputFile, dataFlow);
    }

    public void testLargeFile() throws IOException, ExecutionException {
        checkByFile("File.mod");
    }

    public void testLargeArraySize() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar()
                        VAR num variable{2, 3} := [[0, 1, 2], [3, 4, 5]];
                        variable{1, 1} := 0;
                        variable{0, 2} := 0;
                        variable{3, 2} := 0;
                        variable{2, 0} := 0;
                        variable{2, 4} := 0;
                        variable{3, 4} := 0;
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testArraySize() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar(num{*,*} x)
                        VAR num y{2, 13};
                        x{1,2} := 3;
                        y{1, 1} := 5;
                        y{2, 1} := 5;
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testFunctionCall() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar()
                        VAR num variable := 0;
                        variable := Abs(-1);
                        IF variable = 1 THEN
                        ENDIF
                    ENDPROC
                         
                    FUNC num Abs(num value)
                        IF value >= 0 THEN
                            return value;
                        ELSE
                            return -value;
                        ENDIF
                    ENDFUNC
                ENDMODULE
                """);
    }

    public void testMutuallyExclusiveArgument() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar(\\num x | num y)
                        VAR num z := 0;
                        IF Present(x) THEN
                            z := y + x;
                        ELSE
                            z := y - x;
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testMissingVariable() throws IOException, ExecutionException {
        checkByText("""
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

    public void testLoop1() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar(num n)
                        VAR num i := 3;
                        WHILE i < n DO
                            i := i + 4
                        ENDWHILE
                        IF i = 15 THEN
                            RETURN;
                        ELSE
                            RETURN;
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testLoop2() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    FUNC num bar(num{*} A, num n, num x)
                        VAR num i;
                        WHILE i < n DO
                            IF A{i} = x THEN
                                RETURN i;
                            ELSE
                                i := i + 1;
                            ENDIF
                        ENDWHILE
                        RETURN -1;
                    ENDFUNC
                ENDMODULE
                """);
    }

    public void testArray() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar(num x)
                        VAR num variable{2, 3} := [[0, 1, 2], [3, 4, 5]];
                        IF (variable{1, 3} * variable{2, 2}) = 8 THEN
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }
}
