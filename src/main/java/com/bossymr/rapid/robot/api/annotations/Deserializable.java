package com.bossymr.rapid.robot.api.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that this enum constant can be serialized and deserialized to the specified value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Deserializable {

    /**
     * Specifies that serialized value of this enum constant.
     *
     * @return the serialized value of this enum constant.
     */
    @NotNull String[] value();
}
