package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.RapidTestCase;
import com.bossymr.rapid.RobotTest;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.io.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

class DataFlowGraphTest extends RapidTestCase {

    @BeforeEach
    void setUp() {
        try {
            RobotService.getInstance().disconnect();
        } catch (IOException | InterruptedException e) {
            fail();
        }
        ControlFlowService.getInstance().reload();
    }

    private void checkByText(TestInfo testInfo, String text) throws IOException, ExecutionException {
        check(testInfo, () -> {
            getFixture().configureByText(RapidFileType.getInstance(), text);
            ControlFlowService service = ControlFlowService.getInstance();
            return ReadAction.compute(() -> service.getDataFlow(getFixture().getProject()));
        });
    }

    private void checkByFile(TestInfo testInfo, String fileName) throws IOException, ExecutionException {
        check(testInfo, () -> {
            getFixture().configureByFile(fileName);
            ControlFlowService service = ControlFlowService.getInstance();
            return service.getDataFlow(getFixture().getProject());
        });
    }

    private void check(TestInfo testInfo, Supplier<Set<ControlFlowBlock>> supplier) throws IOException, ExecutionException {
        Set<ControlFlowBlock> dataFlow = supplier.get();
        String name = testInfo.getDisplayName();
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

    @RobotTest
    @Test
    void largeFile(TestInfo testInfo) throws ExecutionException, InterruptedException, IOException {
        RobotService.getInstance().connect(URI.create("http://localhost"), RobotService.DEFAULT_CREDENTIALS);
        checkByFile(testInfo,"File.mod");
    }

    @Test
    void fieldVariable(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
                MODULE foo
                
                    VAR num queue{100};
                    VAR num index := 1;
                
                    PROC bar()
                        FOR i FROM 2 TO index DO
                            IF i = 3 THEN ENDIF
                            queue{i - 1} := queue{i};
                        ENDFOR
                    ENDPROC
                ENDMODULE
                """);
    }

    @RobotTest
    @Test
    void unknownFunction(TestInfo testInfo) throws IOException, ExecutionException {
        try {
            RobotService.getInstance().connect(URI.create("http://localhost:80"), RobotService.DEFAULT_CREDENTIALS);
        } catch (IOException | InterruptedException e) {
            fail();
        }
        checkByText(testInfo, """
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


    @Test
    void largeArraySize(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void arraySize(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void functionCall(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void mutuallyExclusiveArgument(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void mutuallyExclusiveArgument2(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void missingVariable(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void loop1(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void loop2(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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

    @Test
    void array(TestInfo testInfo) throws IOException, ExecutionException {
        checkByText(testInfo, """
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
