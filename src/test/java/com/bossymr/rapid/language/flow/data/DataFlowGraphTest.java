package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.intellij.execution.ExecutionException;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class DataFlowGraphTest extends BasePlatformTestCase {

    private void check(@NotNull String text) throws IOException, ExecutionException {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        DataFlow dataFlow = ControlFlowService.getInstance().getDataFlow(myFixture.getModule());
        File outputFile = new File("C:\\Users\\Robert Fromholz\\Downloads\\graph.svg");
        DataFlowGraphService.convert(outputFile, dataFlow);
    }

    public void testModule() throws IOException, ExecutionException {
        check("""
                MODULE foo
                    PROC bar(num x)
                        VAR num variable{2, 3} := [[0, 1, 2], [3, 4, 5]];
                        variable{1, x} := variable{1, 3} * variable{2, 2};
                    ENDPROC
                ENDMODULE
                """);
    }
}
