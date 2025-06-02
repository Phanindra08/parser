package edu.charlotte.parser.nodes;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

// Define the AST node class
@Setter
@Getter
public class ASTNode {
    String value;
    List<ASTNode> children;

    public ASTNode() {
        this.value = null;
        this.children = null;
    }

    public ASTNode(String value) {
        this();
        this.value = value;
    }

    // Print the AST with tree structure
    public void printAST(String indent, boolean isLast) {
        String connector = isLast ? "└── " : "├── "; // Decide whether to use └── or ├── for the current node
        System.out.println(indent + connector + value);

        String childIndent = indent + (isLast ? "    " : "│   "); // Determine new indentation for child nodes
        if(children != null) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).printAST(childIndent, i == children.size() - 1); // Recursively print children
            }
        }
    }
}
