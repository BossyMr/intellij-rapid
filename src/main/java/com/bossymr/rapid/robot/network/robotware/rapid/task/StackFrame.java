package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.rapid.robot.network.LineTextRange;
import org.jetbrains.annotations.NotNull;

@Entity("rap-stackframe")
public interface StackFrame extends EntityModel {

    @Property("execlevel")
    @NotNull ExecutionLevel getExecutionLevel();

    @Property("beg-row")
    int getStartRow();

    @Property("beg-col")
    int getStartColumn();

    @Property("end-row")
    int getEndRow();

    @Property("end-col")
    int getEndColumn();

    @Property("stack-url")
    @NotNull String getStack();

    @Property("routine-url")
    @NotNull String getRoutine();

    default @NotNull LineTextRange toTextRange() {
        return new LineTextRange(getRoutine().split("/")[2], getStartRow() - 1, getEndRow() - 1, getStartColumn() - 1, getEndColumn() - 1);
    }
}
