package com.bossymr.rapid.robot.api.annotations;

import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresMastership {
    MastershipType value();
}
