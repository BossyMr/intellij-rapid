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
}
