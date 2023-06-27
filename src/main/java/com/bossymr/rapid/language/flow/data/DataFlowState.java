package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A {@code DataFlowState} represents the state of the program at a specific point.
 * <pre>{@code
 * Block 0:
 * 0: if(value) -> (true: 1, false: 2)
 *
 * Block 1:     // State:
 * 0: x = 0;    // x1 = 0;
 * 1: z = 0;    // z1 = 0;
 * 2: goto 3;
 *
 * Block 2:     // State:
 * 0: x = 1;    // x1 = 0;
 * 1: z = 1;    // z1 = 1;
 * 2: goto 3;
 *
 * Block 3:                         // State 1:         State 2:
 * 0: y = (x == 0);                 // y = (x == 0);    y = (x == 0);
 * 1: if(y) -> (true: 4, false: 5)  // x1 = 0;          x1 = 1;
 *                                  // z1 = 0;          z1 = 1;
 *
 * Block 4:     // State:
 *              // x1 = 0;
 *              // z1 = 0;
 * }</pre>
 */
public record DataFlowState(@NotNull List<Condition> conditions, @NotNull Map<Value.Variable, Value.Variable.Snapshot> snapshots) {

    public @NotNull List<Condition> getConditions(@NotNull Value.Variable value) {
        return conditions.stream()
                .filter(condition -> condition.getVariable().equals(value))
                .toList();
    }
}
