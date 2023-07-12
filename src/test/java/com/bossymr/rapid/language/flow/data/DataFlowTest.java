package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class DataFlowTest extends BasePlatformTestCase {

    private @NotNull DataFlow getDataFlow(@NotNull String text) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        ControlFlowService service = ControlFlowService.getInstance();
        ControlFlow controlFlow = service.getControlFlow(myFixture.getModule());
        return service.getDataFlow(controlFlow);
    }

    public void testSimpleExpression() {
        System.out.println(getDataFlow("""
                MODULE foo
                    FUNC num bar()
                        return 0;
                    ENDFUNC
                ENDMODULE
                """));
    }
}
