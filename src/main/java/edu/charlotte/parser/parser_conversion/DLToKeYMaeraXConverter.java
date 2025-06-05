package edu.charlotte.parser.parser_conversion;

import edu.charlotte.parser.nodes.ASTNode;

import java.util.HashMap;
import java.util.Map;

public class DLToKeYMaeraXConverter {
    private final StringBuilder keYMaeraXOutput;
    private final Map<String, String> DLToKeYMaeraXValuesMapping;

    public DLToKeYMaeraXConverter(ASTNode node) {
        this.keYMaeraXOutput = new StringBuilder();
        this.DLToKeYMaeraXValuesMapping = new HashMap<>();
        this.initializingDLToKeYMaeraXMap();
        this.convertToKeYMaeraXAST(node);
        this.generateKeYMaeraXOutput(node);
    }

    private void initializingDLToKeYMaeraXMap() {
        this.DLToKeYMaeraXValuesMapping.put("==", "=");
        this.DLToKeYMaeraXValuesMapping.put("&&", "&");
        this.DLToKeYMaeraXValuesMapping.put("||", "|");
        this.DLToKeYMaeraXValuesMapping.put("<<", "<");
        this.DLToKeYMaeraXValuesMapping.put(">>", ">");
        this.DLToKeYMaeraXValuesMapping.put("**", "*");
        this.DLToKeYMaeraXValuesMapping.put("<EOF>", "");
    }

    public void convertToKeYMaeraXAST(ASTNode node) {
        if (node == null) {
            return;
        } else if(this.DLToKeYMaeraXValuesMapping.containsKey(node.getValue()))
            node.setValue(this.DLToKeYMaeraXValuesMapping.get(node.getValue()));
        if(node.getChildren() != null) {
            for (ASTNode childNode : node.getChildren())
                this.convertToKeYMaeraXAST(childNode);
        }
    }

    public void generateKeYMaeraXOutput(ASTNode node) {
        for(ASTNode childNode: node.getChildren()) {
            if(childNode.getChildren() == null)
                this.keYMaeraXOutput.append(childNode.getValue()).append(" ");
            else
                this.generateKeYMaeraXOutput(childNode);
        }
    }

    public String getKeYMaeraXOutput() {
        return this.keYMaeraXOutput.toString().trim();
    }
}
