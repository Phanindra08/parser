package edu.charlotte.parser.antlr4_parser.basic_arithmetic;

public class BasicArithmetic3 {
    public static void main(String[] args) {
        double num1 = 10.8;
        double num2 = 12;
        double sum, difference, product, quotient;
        sum = 0;
        difference = 2;
        sum = num1 + num2;
        difference = num1 - num2;
        product = num1 * num2;
        quotient = num1 / num2;

        System.out.println("Sum of the numbers is : " + sum);
        System.out.println("Difference: " + difference);
        System.out.println("Product: " + product);
        System.out.println("Quotient: " + quotient);
    }
}