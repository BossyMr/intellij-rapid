package io.github.bossymr.language.psi;

import io.github.bossymr.language.psi.stubs.*;
import io.github.bossymr.language.psi.stubs.type.*;

public interface RapidStubElementTypes {

    RapidStubElementType<RapidModuleStub, RapidModule> MODULE = new RapidModuleElementType();
    RapidStubElementType<RapidAttributeListStub, RapidAttributeList> ATTRIBUTE_LIST = new RapidAttributeListElementType();
    RapidStubElementType<RapidAliasStub, RapidAlias> ALIAS = new RapidAliasElementType();
    RapidStubElementType<RapidRecordStub, RapidRecord> RECORD = new RapidRecordElementType();
    RapidStubElementType<RapidComponentStub, RapidComponent> COMPONENT = new RapidComponentElementType();

    RapidStubElementType<?, RapidField> FIELD = null;
    RapidStubElementType<?, RapidRoutine> ROUTINE = null;
}
