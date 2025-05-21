package edu.charlotte.parser.antlr4_parser.java8;
import java.util.Scanner;

public class BasicArithmetic {
    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter two numbers:");
//        double num1 = scanner.nextDouble();
//        double num2 = scanner.nextDouble();
        double num1 = 10;
        double num2 = 15;

        double sum = num1 + num2;
        double difference = num1 - num2;
        double product = num1 * num2;
        double quotient = num1 / num2;

        System.out.println("Sum: " + sum);
        System.out.println("Difference: " + difference);
        System.out.println("Product: " + product);
        System.out.println("Quotient: " + quotient);
//        scanner.close();
    }
}