package io.github.bossymr.language.psi;

import io.github.bossymr.language.psi.stubs.RapidAttributeListStub;
import io.github.bossymr.language.psi.stubs.RapidModuleStub;
import io.github.bossymr.language.psi.stubs.impl.RapidAttributeListElementType;
import io.github.bossymr.language.psi.stubs.impl.RapidModuleElementType;

public interface RapidStubElementTypes {

    RapidStubElementType<RapidModuleStub, RapidModule> MODULE = new RapidModuleElementType();
    RapidStubElementType<RapidAttributeListStub, RapidAttributeList> ATTRIBUTE_LIST = new RapidAttributeListElementType();

}
