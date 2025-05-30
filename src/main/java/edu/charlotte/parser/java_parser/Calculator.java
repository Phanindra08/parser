package edu.charlotte.parser.java_parser;

public class Calculator {
    private int result;
    // Constructor
    public Calculator() {
        this.result = 0;
    }
    // addition method
    public void add(int value) {
        result += value;
    }
    // subtract method
    public void subtract(int value) {
        result -= value;
    }
    // multiply method
    public void multiply(int value) {
        result *= value;
    }
    // divide method
    public void divide(int value) {
        if (value != 0) {
            result /= value;
        } else {
            System.out.println("Error: Division by zero!");
        }
    }

    public int getResult() {
        return result;
    }
    // driver method
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        calc.add(5);
        calc.subtract(3);
        calc.multiply(4);
        calc.divide(2);
        System.out.println("Result: " + calc.getResult());
    }
}

