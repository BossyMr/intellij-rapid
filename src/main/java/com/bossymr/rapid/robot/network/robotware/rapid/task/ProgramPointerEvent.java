package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.rapid.robot.network.LineTextRange;
import org.jetbrains.annotations.NotNull;

@Entity("rap-pcp-ev")
public interface ProgramPointerEvent extends EntityModel {

    @Property("BegPosLine")
    int getStartRow();

    @Property("BegPosCol")
    int getStartColumn();

    @Property("EndPosLine")
    int getEndRow();

    @Property("EndPosCol")
    int getEndColumn();

    @Property("module-name")
    @NotNull String getModuleName();

    @Property("routine-name")
    @NotNull String getRoutineName();

    default @NotNull LineTextRange toTextRange() {
        return new LineTextRange(getModuleName(), getStartRow(), getEndRow(), getStartColumn(), getEndColumn());
    }
}
