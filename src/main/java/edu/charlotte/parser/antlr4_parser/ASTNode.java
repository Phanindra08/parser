package edu.charlotte.parser.antlr4_parser;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

// Define the AST node class
@Getter
public class ASTNode {
    String type;  // "Greeting", "ID"
    String value; // "Hello", "John"
    List<ASTNode> children;

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    // Print the AST with tree structure
    public void printAST(String indent, boolean isLast) {
        String connector = isLast ? "└── " : "├── "; // Decide whether to use └── or ├── for the current node
        System.out.println(indent + connector + type + "(" + value + ")");

        String childIndent = indent + (isLast ? "    " : "│   "); // Determine new indentation for child nodes
        for (int i = 0; i < children.size(); i++) {
            children.get(i).printAST(childIndent, i == children.size() - 1); // Recursively print children
        }
    }
}