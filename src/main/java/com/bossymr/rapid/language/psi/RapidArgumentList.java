package com.bossymr.rapid.language.psi;

import java.util.List;

public interface RapidArgumentList extends RapidElement {

    /**
     * Returns the arguments specified in this argument list.
     *
     * @return the arguments specified in this argument list.
     */
    List<RapidArgument> getArguments();

}
