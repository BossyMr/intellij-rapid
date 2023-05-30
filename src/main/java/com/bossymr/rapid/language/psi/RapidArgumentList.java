package com.bossymr.rapid.language.psi;

import java.util.List;

public interface RapidArgumentList extends RapidElement {

    /**
     * Returns the parameters specified in this argument list.
     *
     * @return the parameters specified in this argument list.
     */
    List<RapidArgument> getArguments();

}
