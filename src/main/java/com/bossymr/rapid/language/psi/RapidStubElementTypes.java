package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.psi.stubs.*;
import com.bossymr.rapid.language.psi.stubs.type.*;

public interface RapidStubElementTypes {

    RapidStubElementType<RapidModuleStub, RapidModule> MODULE = new RapidModuleElementType();
    RapidStubElementType<RapidAttributeListStub, RapidAttributeList> ATTRIBUTE_LIST = new RapidAttributeListElementType();
    RapidStubElementType<RapidAliasStub, RapidAlias> ALIAS = new RapidAliasElementType();
    RapidStubElementType<RapidRecordStub, RapidRecord> RECORD = new RapidRecordElementType();
    RapidStubElementType<RapidComponentStub, RapidComponent> COMPONENT = new RapidComponentElementType();
    RapidStubElementType<RapidFieldStub, RapidField> FIELD = new RapidFieldElementType();
    RapidStubElementType<?, RapidRoutine> ROUTINE = new RapidRoutineElementType();

    RapidStubElementType<RapidParameterListStub, RapidParameterList> PARAMETER_LIST = new RapidParameterListElementType();
    RapidStubElementType<RapidParameterGroupStub, RapidParameterGroup> PARAMETER_GROUP = new RapidParameterGroupElementType();
    RapidStubElementType<RapidParameterStub, RapidParameter> PARAMETER = new RapidParameterElementType();
}
