package com.bossymr.flow.constraint;

import org.junit.jupiter.api.Test;

class ConstraintEngineTest {

    @Test
    void isReachable() {
        Reachable reachable = ConstraintEngine.isReachable(null);
        System.out.println(reachable);
    }
}