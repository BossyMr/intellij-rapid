package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.symbol.RapidVariable;
import org.jetbrains.annotations.Nullable;

public interface PhysicalVariable extends RapidVariable, PhysicalSymbol {

    @Nullable RapidTypeElement getTypeElement();

}
