package edu.charlotte.parser.conversions.dl.dreal;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class DlToDRealConverter {
    private static final Map<String, String> DL_TO_D_REAL_VALUES_MAPPING = new HashMap<>();
    private static final List<String> DL_OPERATORS_WITH_TWO_OPERANDS = new ArrayList<>();
    private int numberOfSpaces = 1;

    static {
        DL_TO_D_REAL_VALUES_MAPPING.put("!", "not");
        DL_TO_D_REAL_VALUES_MAPPING.put("&&", "and");
        DL_TO_D_REAL_VALUES_MAPPING.put("||", "or");
        DL_TO_D_REAL_VALUES_MAPPING.put("==", "=");
        DL_TO_D_REAL_VALUES_MAPPING.put("<EOF>", "");
        log.info("DlToDRealConverter static mapping initialized with {} entries.", DL_TO_D_REAL_VALUES_MAPPING.size());

        DL_OPERATORS_WITH_TWO_OPERANDS.add("=");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("<");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("<=");
        DL_OPERATORS_WITH_TWO_OPERANDS.add(">");
        DL_OPERATORS_WITH_TWO_OPERANDS.add(">=");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("!=");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("and");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("or");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("+");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("-");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("*");
        DL_OPERATORS_WITH_TWO_OPERANDS.add("/");
        log.info("DlToDRealConverter static operators with two operands list initialized with {} entries.", DL_OPERATORS_WITH_TWO_OPERANDS.size());

    }

    public DlToDRealConverter() {
        log.info("DlToDRealConverter instance is created.");
    }

    private void convertNodeValues(AstNode node) {
        if (node == null) {
            log.debug("Node is null, skipping value conversion.");
            return;
        }

        String originalValue = node.getValue();
        if (originalValue != null && DL_TO_D_REAL_VALUES_MAPPING.containsKey(originalValue)) {
            String newValue = DL_TO_D_REAL_VALUES_MAPPING.get(originalValue);
            node.setValue(newValue);
            log.debug("Converted node value from '{}' to '{}'.", originalValue, newValue);
        } else
            log.debug("Node value '{}' does not require conversion or is null.", originalValue);

        for (AstNode childNode : node.getChildren())
            this.convertNodeValues(childNode);
    }

    private StringBuilder convertToDRealOutput(AstNode node) {
        if (node == null) {
            log.debug("Attempted to append a null AstNode to dReal output.");
            return new StringBuilder();
        }

        StringBuilder dRealOutputBuilder = new StringBuilder();
        List<AstNode> childNodes = node.getChildren();
        if (childNodes.isEmpty() && node.getValue() != null && !node.getValue().trim().isEmpty()) {
            dRealOutputBuilder.append(node.getValue());
            log.debug("Appended the node value '{}' to dReal output.", node.getValue());
        } else if(childNodes.size() == 3 && DL_OPERATORS_WITH_TWO_OPERANDS.contains(childNodes.get(1).getValue()))
            this.convertToDRealOutputForThreeOperands(dRealOutputBuilder, childNodes);
        else {
            for (AstNode childNode : node.getChildren())
                dRealOutputBuilder.append(this.convertToDRealOutput(childNode));
        }
        return dRealOutputBuilder;
    }

    private void convertToDRealOutputForThreeOperands(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        if(childNodes.get(1).getValue().equals(Constants.DL_NOT_EQUAL_OPERATOR)) {
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append("(").append(Constants.NOT_FOR_D_REAL);
            int indexOfSecondOpenBracket = dRealOutputBuilder.length() + 1;
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append("(").append("=").append(" ");
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(0))).append(" ");
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(2)));
            this.numberOfSpaces--;
            if (dRealOutputBuilder.indexOf("\n", indexOfSecondOpenBracket) != -1)
                dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            dRealOutputBuilder.append(")");
            this.numberOfSpaces--;
            if (dRealOutputBuilder.indexOf("\n", 1) != -1)
                dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            dRealOutputBuilder.append(")");
        } else {
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append("(").append(childNodes.get(1).getValue()).append(" ");
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(0))).append(" ");
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(2)));
            this.numberOfSpaces--;
            if (dRealOutputBuilder.indexOf("\n", 1) != -1)
                dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            dRealOutputBuilder.append(")");
        }
    }

    public String convertDlToDReal(AstNode astRoot) {
        Objects.requireNonNull(astRoot, "Ast root node cannot be null for conversion.");
        log.info("Starting the conversion of AST from DL to dReal format.");

        convertNodeValues(astRoot);
        log.debug("AST node values are converted to dReal values.");

        String outputBuilder = "(assert" + convertToDRealOutput(astRoot) + "\n)";
        log.info("dReal output string is generated.");
        return outputBuilder.trim();
    }
}