package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.debug.ShowDataFlowGraphHandler;
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
        ShowDataFlowGraphHandler.convert(outputFile, dataFlow);
    }

    public void testModule() throws IOException, ExecutionException {
        check("""
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
}
