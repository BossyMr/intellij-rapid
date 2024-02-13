package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.psi.stubs.*;
import com.bossymr.rapid.language.psi.stubs.type.*;
import com.bossymr.rapid.language.symbol.physical.*;

public interface RapidStubElementTypes {

    RapidStubElementType<RapidModuleStub, PhysicalModule> MODULE = new RapidModuleElementType();
    RapidStubElementType<RapidAttributeListStub, RapidAttributeList> ATTRIBUTE_LIST = new RapidAttributeListElementType();
    RapidStubElementType<RapidAliasStub, PhysicalAlias> ALIAS = new RapidAliasElementType();
    RapidStubElementType<RapidRecordStub, PhysicalRecord> RECORD = new RapidRecordElementType();
    RapidStubElementType<RapidComponentStub, PhysicalComponent> COMPONENT = new RapidComponentElementType();
    RapidStubElementType<RapidFieldStub, PhysicalField> FIELD = new RapidFieldElementType();
    RapidStubElementType<RapidRoutineStub, PhysicalRoutine> ROUTINE = new RapidRoutineElementType();

    RapidStubElementType<RapidParameterListStub, RapidParameterList> PARAMETER_LIST = new RapidParameterListElementType();
    RapidStubElementType<RapidParameterGroupStub, PhysicalParameterGroup> PARAMETER_GROUP = new RapidParameterGroupElementType();
    RapidStubElementType<RapidParameterStub, PhysicalParameter> PARAMETER = new RapidParameterElementType();
}
