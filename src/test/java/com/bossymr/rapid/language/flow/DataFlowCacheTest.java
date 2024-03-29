package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiDocumentManager;
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
            service.getDataFlow(routine);
        });
        assertEquals(processed, result);
    }

    private void checkByTextAfterModification(@NotNull String text, @NotNull Set<String> processed) {
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
        service.getDataFlow(myFixture.getProject());
        result.clear();
        myFixture.type(' ');
        PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();
        ReadAction.run(() -> service.getDataFlow(myFixture.getProject()));
        assertEquals(processed, result);
    }

    public void testModificationCache() {
        checkByTextAfterModification("""
                MODULE foo
                    PROC bar()
                        <caret>
                    ENDPROC
                    
                    PROC baz()
                    ENDPROC
                ENDMODULE
                """, Set.of("foo:bar"));
    }

    public void testModificationCacheWithDependency() {
        checkByTextAfterModification("""
                MODULE foo
                    FUNC num bar()
                        <caret>
                        RETURN 0;
                    ENDPROC
                    
                    PROC baz()
                        VAR value := 0;
                        value := bar();
                    ENDPROC
                ENDMODULE
                """, Set.of("foo:baz", "foo:bar"));
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
                """, Set.of("foo:bar"));
    }

    public void testChainWithReturnValue() {
        checkByText("""
                MODULE foo
                    PROC bar()
                        <caret>
                        VAR value := bar2();
                    ENDPROC
                    
                    FUNC num bar2()
                        RETURN -1;
                    ENDFUNC
                ENDMODULE
                """, Set.of("foo:bar", "foo:bar2"));
    }

    public void testChainWithUnusedReturnValue() {
        checkByText("""
                MODULE foo
                    PROC bar()
                        <caret>
                        bar2();
                    ENDPROC
                    
                    FUNC num bar2()
                        RETURN -1;
                    ENDFUNC
                ENDMODULE
                """, Set.of("foo:bar"));
    }

    public void testChainWithError() {
        checkByText("""
                MODULE foo
                    PROC bar()
                        <caret>
                        bar2();
                    ENDPROC
                    
                    FUNC num bar2()
                        RAISE 1;
                    ENDFUNC
                ENDMODULE
                """, Set.of("foo:bar", "foo:bar2"));
    }

    public void testDeepChainWithReturnValue() {
        checkByText("""
                MODULE foo
                    PROC bar()
                        <caret>
                        VAR value := bar2();
                    ENDPROC
                    
                    FUNC num bar2()
                        RETURN bar3();
                    ENDFUNC
                    
                    FUNC num bar3()
                        RETURN -1;
                    ENDFUNC
                ENDMODULE
                """, Set.of("foo:bar", "foo:bar2", "foo:bar3"));
    }

}
