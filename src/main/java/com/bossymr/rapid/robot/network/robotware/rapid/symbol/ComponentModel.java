package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;

@Entity("rap-sympropreccomp")
public interface ComponentModel extends SymbolModel, TypeSymbol {

    /**
     * Returns the index of this component in the record. The index of the first component is 1.
     *
     * @return the index (starting at 1) of this component.
     */
    @Property("comnum")
    int getIndex();

}
