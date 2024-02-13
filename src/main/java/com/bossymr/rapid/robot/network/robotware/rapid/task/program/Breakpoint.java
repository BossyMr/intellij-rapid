package com.bossymr.rapid.robot.network.robotware.rapid.task.program;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.rapid.robot.network.LineTextRange;
import org.jetbrains.annotations.NotNull;

@Entity("rap-program-breakpoint")
public interface Breakpoint {

    @Property("module-name")
    @NotNull String getModuleName();

    @Property("start-row")
    int getStartRow();

    @Property("end-row")
    int getEndRow();

    @Property("start-col")
    int getStartColumn();

    @Property("end-col")
    int getEndColumn();

    default @NotNull LineTextRange toTextRange() {
        return new LineTextRange(getModuleName(), getStartRow() - 1, getEndRow() - 1, getStartColumn() - 1, getEndColumn() - 1);
    }

}
