package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    void shouldAddTwoNumbers() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    void shouldSubtractTwoNumbers() {
        assertEquals(2, calculator.subtract(5, 3));
    }

    @Test
    void shouldMultiplyTwoNumbers() {
        assertEquals(15, calculator.multiply(3, 5));
    }

    @Test
    void shouldDivideTwoNumbers() {
        assertEquals(2.5, calculator.divide(5, 2));
    }

    @Test
    void shouldThrowWhenDividingByZero() {
        assertThrows(ArithmeticException.class, () -> calculator.divide(1, 0));
    }
}
