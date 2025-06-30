package edu.charlotte.parser.conversions.dl.keymaerax;

import edu.charlotte.parser.ast.nodes.AstNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class DlToKeYmaeraXConverter {
    private static final Map<String, String> DL_TO_KEYMAERAX_VALUES_MAPPING = new HashMap<>();

    static {
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("==", "=");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("&&", "&");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("||", "|");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("<<", "<");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put(">>", ">");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("**", "*");
        DL_TO_KEYMAERAX_VALUES_MAPPING.put("<EOF>", "");
        log.info("DlToKeYmaeraXConverter static mapping initialized with {} entries.", DL_TO_KEYMAERAX_VALUES_MAPPING.size());
    }

    public DlToKeYmaeraXConverter() {
        log.info("DlToKeYmaeraXConverter instance is created.");
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

    private void appendKeYmaeraXOutput(AstNode node, StringBuilder keYmaeraXOutputBuilder) {
        if (node == null) {
            log.debug("Attempted to append a null AstNode to KeYmaeraX output.");
            return;
        }

        if (node.getChildren().isEmpty() && node.getValue() != null && !node.getValue().trim().isEmpty()) {
            keYmaeraXOutputBuilder.append(node.getValue()).append(" ");
            log.debug("Appended the node value '{}' to KeYmaeraX output.", node.getValue());
        } else {
            for (AstNode childNode : node.getChildren())
                this.appendKeYmaeraXOutput(childNode, keYmaeraXOutputBuilder);
        }
    }

    public String convertDlToKeYmaeraX(AstNode astRoot) {
        Objects.requireNonNull(astRoot, "Ast root node cannot be null for conversion.");
        log.info("Starting the conversion of AST from DL to KeYmaeraX format.");

        convertNodeValues(astRoot);
        log.debug("AST node values are converted to KeYmaeraX values.");

        StringBuilder keYmaeraXOutputBuilder = new StringBuilder();
        appendKeYmaeraXOutput(astRoot, keYmaeraXOutputBuilder);
        log.info("KeYmaeraX output string is generated.");
        return keYmaeraXOutputBuilder.toString().trim();
    }
}