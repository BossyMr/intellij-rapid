package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.RapidTestCase;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFlowCacheTest extends RapidTestCase {

    private void checkByText(@NotNull String text, @NotNull Set<String> processed) {
        getFixture().configureByText(RapidFileType.getInstance(), text);
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
            PsiElement element = getFixture().getFile().findElementAt(getFixture().getCaretOffset());
            Objects.requireNonNull(element);
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(element);
            Objects.requireNonNull(routine);
            service.getDataFlow(routine);
        });
        assertEquals(processed, result);
    }

    private void checkByTextAfterModification(@NotNull String text, @NotNull Set<String> processed) {
        getFixture().configureByText(RapidFileType.getInstance(), text);
        Set<String> result = new HashSet<>();
        ControlFlowListener.connect(new ControlFlowListener() {
            @Override
            public void onBlock(@NotNull ControlFlowBlock block) {
                Block controlFlow = block.getControlFlow();
                result.add(controlFlow.getModuleName() + ":" + controlFlow.getName());
            }
        });
        ControlFlowService service = ControlFlowService.getInstance();
        ReadAction.run(() -> service.getDataFlow(getFixture().getProject()));
        result.clear();
        getFixture().type(' ');
        PsiDocumentManager.getInstance(getFixture().getProject()).commitAllDocuments();
        ReadAction.run(() -> service.getDataFlow(getFixture().getProject()));
        assertEquals(processed, result);
    }

    @Test
    void modificationCache() {
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

    @Test
    void modificationCacheWithDependency() {
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

    @Test
    void unusedRoutine() {
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

    @Test
    void disconnectedChain() {
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

    @Test
    void chainWithReturnValue() {
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

    @Test
    void chainWithUnusedReturnValue() {
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

    @Test
    void chainWithError() {
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

    @Test
    void deepChainWithReturnValue() {
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
