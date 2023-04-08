package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MastershipException extends RuntimeException {

    private final @NotNull Mastership mastership;

    public MastershipException(@NotNull Mastership mastership) {
        super("Mastership '" + mastership + "' already held");
        this.mastership = mastership;
    }

    public @NotNull Mastership getMastership() {
        return mastership;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MastershipException that = (MastershipException) o;
        return getMastership() == that.getMastership();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMastership());
    }

    @Override
    public String toString() {
        return "MastershipException{" +
                "mastership=" + mastership +
                '}';
    }
}
