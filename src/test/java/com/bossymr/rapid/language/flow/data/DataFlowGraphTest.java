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
        DataFlow dataFlow = ControlFlowService.getInstance().getDataFlow(myFixture.getProject());
        File outputFile = new File("%UserProfile%\\Downloads\\graph.svg");
        DataFlowGraphService.convert(outputFile, dataFlow);
    }

    public void testModule() throws IOException, ExecutionException {
        check("""
                MODULE DrawModule (SYSMODULE, NOVIEW)
                    
                    PROC DrawSquare(VAR num size)
                    ENDPROC
                    
                    PROC DrawSquares(robtarget target, num size, num amount)
                        FOR i FROM 1 TO amount DO
                            DrawSquare target, size;
                        ENDFOR
                    ENDPROC
                ENDMODULE
                """);
    }
}
