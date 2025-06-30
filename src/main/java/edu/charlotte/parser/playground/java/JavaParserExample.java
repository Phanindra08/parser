package edu.charlotte.parser.playground.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.io.File;
import java.util.List;

public class JavaParserExample {
    public static void main(String[] args) {
        // Sample Java code to parse
//        File sourceFile = new File("src/main/java/edu/charlotte/parser/java_parser/GFG.java");
        File sourceFile = new File("src/main/java/edu/charlotte/parser/java_parser/Calculator.java");

        try {
            // Parse the code into an AST
            CompilationUnit cu = StaticJavaParser.parse(sourceFile);
            // Print the entire AST
            printAST(cu, "", true);
        } catch (Exception e) {
            System.err.println("Error parsing code: " + e.getMessage());
        }
    }

    // Recursive method to print the AST in a tree-like structure
    private static void printAST(Node node, String indent, boolean isLast) {
        String connector = isLast ? "└── " : "├── "; // Print the node in a tree-like format
        if (node instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) node;
            System.out.println(indent + connector + "  Method call: " + methodCall.getName()); // Print method name
        } else
            System.out.println(indent + connector + node.getClass().getSimpleName() + ": " + node.toString().trim()); // Print other nodes
        String childIndent = indent + (isLast ? "    " : "│   "); // Determine the new indentation for the children

        // Recurrence for each child node
        List<Node> childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.size(); i++) {
            printAST(childNodes.get(i), childIndent, i == childNodes.size() - 1); // Last child gets different indent
        }
    }
}