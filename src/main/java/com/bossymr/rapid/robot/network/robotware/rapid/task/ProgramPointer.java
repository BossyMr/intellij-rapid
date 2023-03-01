package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.rapid.robot.network.LineTextRange;
import org.jetbrains.annotations.NotNull;

@Entity("pcp-info")
public interface ProgramPointer extends EntityModel {

    /**
     * Returns the start position of the pointer.
     *
     * @return the start position, 'row,column' of the pointer.
     */
    @Property({"beginposition", "begposition"})
    @NotNull String getStartPosition();

    /**
     * Returns the end position of the pointer.
     *
     * @return the end position, 'row,column' of the pointer.
     */
    @Property("endposition")
    @NotNull String getEndPosition();

    @Property("modulemame")
    @NotNull String getModuleName();

    @Property("routinename")
    @NotNull String getRoutineName();

    @Property("changecount")
    int getChangeCount();

    default @NotNull LineTextRange toTextRange() {
        String[] startPosition = getStartPosition().split(",");
        String[] endPosition = getEndPosition().split(",");
        return new LineTextRange(getModuleName(), Integer.parseInt(startPosition[0]) - 1, Integer.parseInt(endPosition[0]) - 1, Integer.parseInt(startPosition[1]) - 1, Integer.parseInt(endPosition[1]) - 1);
    }

}
