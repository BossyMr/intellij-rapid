package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataFlowGraphTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            RobotService.getInstance().disconnect();
        } catch (IOException | InterruptedException e) {
            fail();
        }
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
        String output = ControlFlowFormatVisitor.format(dataFlow.stream().map(ControlFlowBlock::getControlFlow).collect(Collectors.toSet()));
        FileUtil.writeToFile(path.resolve("controlFlow.txt").toFile(), output);
        File outputFile = path.resolve("dataFlow.svg").toFile();
        DataFlowGraphService.convert(outputFile, dataFlow);
    }

    public void testLargeFile() throws IOException, ExecutionException, InterruptedException {
        RobotService.getInstance().connect(URI.create("http://localhost"), RobotService.DEFAULT_CREDENTIALS);
        checkByFile("File.mod");
    }

    public void testFieldVariable() throws IOException, ExecutionException {
        checkByText("""
                MODULE TestModule
                                
                    VAR num queue{100};
                    VAR num index := 1;
                                
                    PROC DUMMY()
                        FOR i FROM 2 TO index DO
                            IF i = 3 THEN ENDIF
                            queue{i - 1} := queue{i};
                        ENDFOR
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testUnknownFunction() throws IOException, ExecutionException {
        try {
            RobotService.getInstance().connect(URI.create("http://localhost:80"), RobotService.DEFAULT_CREDENTIALS);
        } catch (IOException | InterruptedException e) {
            fail();
        }
        checkByText("""
                MODULE foo
                    FUNC num askChoice(string question, string choice1, string choice2, string choice3, string choice4, string choice5)
                        VAR num value;
                   
                        TPReadFK value, question, choice1, choice2, choice3, choice4, choice5;
                        IF value = 0 THEN
                            value := value + 1;
                        ENDIF
                        RETURN value;
                    ENDFUNC
                ENDMODULE
                """);
    }


    public void testLargeArraySize() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar()
                        VAR num variable{2, 3} := [[0, 1, 2], [3, 4, 5]];
                        VAR num variable2{2 + 3};
                        VAR num index := 3;
                        variable{1, 1} := 0;
                        variable{0, 2} := 0;
                        variable{3, 2} := 0;
                        variable{2, 0} := 0;
                        variable{2, 4} := 0;
                        variable2{1.5} := 2;
                        variable{index, 4} := 0;
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

    public void testMutuallyExclusiveArgument2() throws IOException, ExecutionException {
        checkByText("""
                MODULE foo
                    PROC bar(\\num x | num y | num z)
                        IF Present(x) THEN
                            z := y + x + z;
                        ELSE
                            z := y - x - z;
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
