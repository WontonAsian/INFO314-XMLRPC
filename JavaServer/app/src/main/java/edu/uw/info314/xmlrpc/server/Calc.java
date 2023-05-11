package edu.uw.info314.xmlrpc.server;

public class Calc {
    public int add(int... args) {
        int result = 0;
        for (int arg : args) {
            try {
                result = Math.addExact(result, arg);
            } catch(ArithmeticException e) {
                throw new ArithmeticException("overflow");
            }
        }
        return result;
    }

    public int subtract(int lhs, int rhs) { return lhs - rhs; }

    public int multiply(int... args) {
        int result = 1; // Initialize to 1 for multiplication
        for (int arg : args) {
            try {
                result = Math.multiplyExact(result, arg);
            } catch(ArithmeticException e) {
                throw new ArithmeticException("overflow");
            }
        }
        return result;
    }

    public int divide(int lhs, int rhs) {
        if (rhs == 0) {
            throw new ArithmeticException("divide by zero"); // Throw an exception for divide by zero
        }
        return lhs / rhs;
    }

    public int modulo(int lhs, int rhs) {
        if (rhs == 0) {
            throw new ArithmeticException("divide by zero"); // Throw an exception for modulo by zero
        }
        return lhs % rhs;
    }
}
