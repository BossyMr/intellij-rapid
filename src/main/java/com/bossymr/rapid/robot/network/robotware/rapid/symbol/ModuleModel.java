package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;

@Entity("rap-sympropmod")
public interface ModuleModel extends SymbolModel {

    @Property("changed")
    boolean isChanged();

    @Property("encoded")
    boolean isEncoded();

    @Property("nostepin")
    boolean isNoStepIn();

    @Property("noview")
    boolean isNoView();

    @Property("rdonly")
    boolean isReadOnly();

    @Property("syssmod")
    boolean isSystemModule();

    @Property("viewonly")
    boolean isViewOnly();


}
