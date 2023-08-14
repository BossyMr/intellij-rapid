package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.flow.constraint.NumericConstraint.Bound;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void missingBound() {
        assertThrows(IllegalArgumentException.class, () -> new Range(new Bound(true, 10), new Bound(true, 0)));
        assertThrows(IllegalArgumentException.class, () -> new Range(new Bound(false, 0), new Bound(true, 0)));
        assertThrows(IllegalArgumentException.class, () -> new Range(new Bound(true, 0), new Bound(false, 0)));
    }

    @Test
    void getPoint() {
        Range withPoint = new Range(new Bound(true, 0), new Bound(true, 0));
        assertEquals(0, withPoint.getPoint().orElseThrow());
        Range withBound = new Range(new Bound(true, 0), new Bound(true, 10));
        assertTrue(withBound.getPoint().isEmpty());
    }

    @Test
    void containsRange() {
        Range range = new Range(new Bound(true, 0), new Bound(false, 10));
        assertTrue(range.contains(new Range(new Bound(true, 2), new Bound(true, 5))));
        assertFalse(range.contains(new Range(new Bound(true, 15), new Bound(true, 20))));
        assertTrue(range.contains(new Range(new Bound(true, 0), new Bound(false, 10))));
        assertTrue(range.contains(new Range(new Bound(false, 0), new Bound(false, 10))));
        assertFalse(range.contains(new Range(new Bound(true, 0), new Bound(true, 10))));
        assertFalse(range.contains(new Range(new Bound(true, 0), new Bound(false, 15))));
    }

    @Test
    void isSmallerRange() {
        Range range = new Range(new Bound(false, 0), new Bound(false, 10));
        assertTrue(range.isSmaller(new Range(new Bound(true, -10), new Bound(false, 0))).orElseThrow());
        assertTrue(range.isSmaller(new Range(new Bound(true, -10), new Bound(true, 0))).orElseThrow());
        assertTrue(range.isSmaller(new Range(new Bound(true, -10), new Bound(true, 5))).isEmpty());
        assertTrue(range.isSmaller(new Range(new Bound(true, 10), new Bound(true, 15))).orElseThrow());
    }

    @Test
    void intersectionRange() {
        assertIntersection(new Range(new Bound(true, 5), new Bound(true, 10)), new Range(new Bound(true, 5), new Bound(true, 15)), new Range(new Bound(true, 5), new Bound(true, 15)));
    }

    private void assertIntersection(@Nullable Range expected, @NotNull Range a, @NotNull Range b) {
        Optional<Range> intersect = a.intersect(b);
        if(expected == null) {
            assertTrue(intersect.isEmpty());
        } else {
            assertEquals(expected, intersect.orElseThrow());
        }
    }

    @Test
    void intersectsRange() {
        assertTrue(range(true, 0, true, 10).intersects(range(true, 5, true, 15)));
        assertTrue(range(true, 0, true, 10).intersects(range(true, 0, true, 10)));
        assertTrue(range(true, 0, true, 10).intersects(range(false, 0, false, 10)));
        assertFalse(range(true, 0, false, 10).intersects(range(true, 10, true, 20)));
        assertFalse(range(true, 0, true, 10).intersects(range(true, 20, true, 30)));
    }

    private @NotNull Range range(boolean isInclusiveA, double valueA, boolean isInclusiveB, double valueB) {
        return new Range(new Bound(isInclusiveA, valueA), new Bound(isInclusiveB, valueB));
    }
}