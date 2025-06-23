package edu.charlotte.parser.parser_conversions.dl_to_keymaerax_conversion;

import edu.charlotte.parser.ast.nodes.AstNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class DLToKeYMaeraXConverter {
    private static final Map<String, String> DL_TO_KEYMAERAX_VALUES_MAPPING = new HashMap<>();

    static {
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("==", "=");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("&&", "&");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("||", "|");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("<<", "<");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put(">>", ">");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("**", "*");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("<EOF>", "");
        log.info("DLToKeYMaeraXConverter static mapping initialized with {} entries.", DL_TO_KEYMAERAX_VALUES_MAPPING.size());
    }

    public DLToKeYMaeraXConverter() {
        log.info("DLToKeYMaeraXConverter instance is created.");
    }

    private void convertNodeValues(AstNode node) {
        if (node == null) {
            log.debug("Node is null, skipping value conversion.");
            return;
        }

        String originalValue = node.getValue();
        if (originalValue != null && DL_TO_KEYMAERAX_VALUES_MAPPING.containsKey(originalValue)) {
            String newValue = DL_TO_KEYMAERAX_VALUES_MAPPING.get(originalValue);
            node.setValue(newValue);
            log.debug("Converted node value from '{}' to '{}'.", originalValue, newValue);
        } else
            log.debug("Node value '{}' does not require conversion or is null.", originalValue);

        for (AstNode childNode : node.getChildren())
            this.convertNodeValues(childNode);
    }

    private void appendKeYMaeraXOutput(AstNode node, StringBuilder keYMaeraXOutputBuilder) {
        if (node == null) {
            log.debug("Attempted to append a null ASTNode to KeYMaeraX output.");
            return;
        }

        if (node.getChildren().isEmpty() && node.getValue() != null && !node.getValue().trim().isEmpty()) {
            keYMaeraXOutputBuilder.append(node.getValue()).append(" ");
            log.debug("Appended the node value '{}' to KeYMaeraX output.", node.getValue());
        } else {
            for (AstNode childNode : node.getChildren())
                this.appendKeYMaeraXOutput(childNode, keYMaeraXOutputBuilder);
        }
    }

    public String convertDLToKeYMaeraX(AstNode astRoot) {
        Objects.requireNonNull(astRoot, "AST root node cannot be null for conversion.");
        log.info("Starting the conversion of AST from DL to KeYMaeraX format.");

        convertNodeValues(astRoot);
        log.debug("AST node values are converted to KeYMaeraX values.");

        StringBuilder keYMaeraXOutputBuilder = new StringBuilder();
        appendKeYMaeraXOutput(astRoot, keYMaeraXOutputBuilder);
        log.info("KeYMaeraX output string is generated.");
        return keYMaeraXOutputBuilder.toString().trim();
    }
}