package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.bossymr.rapid.language.psi.RapidRecord;

public interface RapidRecordStub extends NamedStub<RapidRecord> {
    /**
     * Checks if the record is local.
     *
     * @return if the record is local.
     */
    boolean isLocal();
}
