package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DataFlowCacheTest extends BasePlatformTestCase {

    private void checkByText(@NotNull String text, @NotNull Set<String> processed) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        Set<String> result = new HashSet<>();
        ControlFlowListener.connect(new ControlFlowListener() {
            @Override
            public void onBlock(@NotNull ControlFlowBlock block) {
                Block controlFlow = block.getControlFlow();
                result.add(controlFlow.getModuleName() + ":" + controlFlow.getName());
            }
        });
        ControlFlowService service = ControlFlowService.getInstance();
        ReadAction.run(() -> {
            PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
            Objects.requireNonNull(element);
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(element);
            Objects.requireNonNull(routine);
            service.getControlFlowBlock(routine);
        });
        assertEquals(processed, result);
    }

    public void testUnusedRoutine() {
        checkByText("""
                MODULE foo
                    PROC bar()
                        <caret>
                    ENDPROC
                    
                    PROC baz()
                    ENDPROC
                ENDMODULE
                """, Set.of("foo:bar"));
    }

    public void testDisconnectedChain() {
        checkByText("""
                MODULE foo
                    PROC bar()
                        <caret>
                        bar2;
                    ENDPROC
                    
                    PROC bar2()
                        bar3;
                        bar4;
                    ENDPROC
                    
                    PROC bar3()
                    ENDPROC
                    
                    PROC bar4()
                    ENDPROC
                    
                    PROC baz()
                        baz2;
                    ENDPROC
                    
                    PROC baz2()
                    ENDPROC
                ENDMODULE
                """, Set.of("foo:bar", "foo:bar2", "foo:bar3", "foo:bar4"));
    }

}
