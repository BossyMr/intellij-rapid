package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("pcp-info")
public interface ProgramPointer extends EntityModel {

    /**
     * Returns the start position of the pointer.
     *
     * @return the start position, '[ROW]/[COLUMN]' of the pointer.
     */
    @Property({"beginposition", "begposition"})
    @NotNull String getStartPosition();

    /**
     * Returns the end position of the pointer.
     *
     * @return the end position, '[ROW]/[COLUMN]' of the pointer.
     */
    @Property("endposition")
    @NotNull String getEndPosition();

    @Property("modulemame")
    @NotNull String getModule();

    @Property("routinename")
    @NotNull String getRoutine();

    @Property("changecount")
    int getChangeCount();

}
