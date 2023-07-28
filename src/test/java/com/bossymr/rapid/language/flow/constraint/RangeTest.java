package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.flow.constraint.NumericConstraint.Bound;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint.Range;
import org.junit.jupiter.api.Test;

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
    void intersectsRange() {
        Range range = new Range(new Bound(true, 0), new Bound(true, 10));
        Range intersection = range.intersect(new Range(new Bound(true, 5), new Bound(true, 15)));
        assertEquals(new Range(new Bound(true, 5), new Bound(true, 10)), intersection);
    }
}