package edu.charlotte.parser.nodes;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Define the AST node class
@Getter
@Slf4j
public class ASTNode {
    @Setter
    private String value;
    private final List<ASTNode> children;

    public ASTNode() {
        this.value = null;
        this.children = new ArrayList<>();
    }

    public ASTNode(String value) {
        this.value = Objects.requireNonNull(value, "ASTNode value cannot be null upon construction.");
        this.children = new ArrayList<>();
    }

    public void addChildren(List<ASTNode> childrenNodes) {
        Objects.requireNonNull(childrenNodes, "List of children nodes to be added cannot be null.");
        this.children.addAll(childrenNodes);
        log.debug("Added '{}' children to the node '{}'", childrenNodes.size(), this.value);
    }

    // Generating the AST in a tree structure
    public void generateASTTree(String indent, boolean isLast, StringBuilder astOutputTree) {
        String connector = isLast ? "└── " : "├── "; // Decide whether to use └── (last child) or ├── (middle child) for the current node.
        astOutputTree.append(indent).append(connector).append(value).append("\n");
        log.debug("Processed AST tree node: '{}'", value);
        String childIndent = indent + (isLast ? "    " : "│   "); // Determine new indentation for child nodes
        for (int i = 0; i < children.size(); i++)
            children.get(i).generateASTTree(childIndent, i == children.size() - 1, astOutputTree);
    }

    @Override
    public String toString() {
        return "ASTNode(value='" + value + "', childrenCount=" + children.size() + ")";
    }
}