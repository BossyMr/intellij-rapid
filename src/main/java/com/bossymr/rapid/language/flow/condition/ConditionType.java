package com.bossymr.rapid.language.flow.condition;

import org.jetbrains.annotations.NotNull;

public enum ConditionType {
    EQUALITY, INEQUALITY, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL;

    public @NotNull ConditionType negate() {
        return switch (this) {
            case EQUALITY -> INEQUALITY;
            case INEQUALITY -> EQUALITY;
            case LESS_THAN -> GREATER_THAN_OR_EQUAL;
            case LESS_THAN_OR_EQUAL -> GREATER_THAN;
            case GREATER_THAN -> LESS_THAN_OR_EQUAL;
            case GREATER_THAN_OR_EQUAL -> LESS_THAN;
        };
    }

    public @NotNull ConditionType flip() {
        return switch (this) {
            case EQUALITY -> EQUALITY;
            case INEQUALITY -> INEQUALITY;
            case LESS_THAN -> GREATER_THAN;
            case LESS_THAN_OR_EQUAL -> GREATER_THAN_OR_EQUAL;
            case GREATER_THAN -> LESS_THAN;
            case GREATER_THAN_OR_EQUAL -> LESS_THAN_OR_EQUAL;
        };
    }
}
