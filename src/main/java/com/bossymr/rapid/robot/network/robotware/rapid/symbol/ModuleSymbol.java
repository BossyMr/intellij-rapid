package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;

@Entity({"rap-sympropmod", "rap-sympropmod-li"})
public interface ModuleSymbol extends Symbol {

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
