package io.github.bossymr.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import io.github.bossymr.language.psi.RapidRecord;

public interface RapidRecordStub extends NamedStub<RapidRecord> {
    /**
     * Checks if the record is local.
     *
     * @return if the record is local.
     */
    boolean isLocal();
}
