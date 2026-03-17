package com.simulator.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2DTest {

    @Test
    void normalizedOfZeroReturnsZeroVector() {
        Vector2D normalized = Vector2D.ZERO.normalized();
        assertEquals(0.0, normalized.getX(), 1e-12);
        assertEquals(0.0, normalized.getY(), 1e-12);
    }

    @Test
    void vectorArithmeticWorksAsExpected() {
        Vector2D a = new Vector2D(3.0, 4.0);
        Vector2D b = new Vector2D(-1.0, 2.0);

        Vector2D sum = a.add(b);
        Vector2D diff = a.subtract(b);
        Vector2D scaled = a.multiply(2.0);

        assertEquals(2.0, sum.getX(), 1e-12);
        assertEquals(6.0, sum.getY(), 1e-12);

        assertEquals(4.0, diff.getX(), 1e-12);
        assertEquals(2.0, diff.getY(), 1e-12);

        assertEquals(6.0, scaled.getX(), 1e-12);
        assertEquals(8.0, scaled.getY(), 1e-12);
        assertEquals(5.0, a.magnitude(), 1e-12);
    }
}

