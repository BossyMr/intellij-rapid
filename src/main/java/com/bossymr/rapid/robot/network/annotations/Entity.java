package com.bossymr.rapid.robot.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this interface is an entity. This annotation should only be annotated on interfaces.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    /**
     * Specifies the type of this entity. A response will only be converted to this type if the response type is equal
     * to one of the types of this entity.
     *
     * @return the type of this entity.
     */
    String @NotNull [] value() default {};

    /**
     * Specifies the subtypes of this entity. However, a response will still be converted to this type if the response
     * type is equal to one of the types of this entity.
     *
     * @return the subtypes of this entity.
     */
    Class<?> @NotNull [] subtype() default {};
}
