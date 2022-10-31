package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidField;

import java.util.List;

public interface RapidFieldList extends RapidElement {

    /**
     * Returns the fields in this field list.
     *
     * @return the fields in this field list.
     */
    List<RapidField> getFields();

}
