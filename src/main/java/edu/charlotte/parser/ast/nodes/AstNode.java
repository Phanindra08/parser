package edu.charlotte.parser.ast.nodes;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Define the AstNode class
@Getter
@Slf4j
public class AstNode {
    @Setter
    private String value;
    private final List<AstNode> children;

    public AstNode() {
        this.value = null;
        this.children = new ArrayList<>();
    }

    public AstNode(String value) {
        this.value = Objects.requireNonNull(value, "AstNode value cannot be null upon construction.");
        this.children = new ArrayList<>();
    }

    public AstNode(String value, List<AstNode> children) {
        this.value = Objects.requireNonNull(value, "AstNode value cannot be null upon construction.");
        this.children = Objects.requireNonNull(children, "AstNode children cannot be null upon construction.");
    }

    public void addChildren(List<AstNode> childrenNodes) {
        Objects.requireNonNull(childrenNodes, "List of children nodes to be added cannot be null.");
        this.children.addAll(childrenNodes);
        log.debug("Added '{}' children to the node '{}'", childrenNodes.size(), this.value);
    }

    // Generating the AST in a tree structure
    public void generateAstTree(String indent, boolean isLast, StringBuilder astOutputTree) {
        String connector = isLast ? "└── " : "├── "; // Decide whether to use └── (last child) or ├── (middle child) for the current node.
        astOutputTree.append(indent).append(connector).append(value).append("\n");
        log.debug("Processed AST tree node: '{}'", value);
        String childIndent = indent + (isLast ? "    " : "│   "); // Determine new indentation for child nodes
        for (int i = 0; i < children.size(); i++)
            children.get(i).generateAstTree(childIndent, i == children.size() - 1, astOutputTree);
    }

    @Override
    public String toString() {
        return "AstNode(value='" + value + "', childrenCount=" + children.size() + ")";
    }
}