package edu.charlotte.parser.conversions.dl.dreal;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.utils.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class DlToDRealConverter {
    private static final Map<String, String> DL_TO_D_REAL_VALUES_MAPPING = new HashMap<>();
    private static final List<String> DL_OPERATORS_WITH_TWO_OPERANDS = new ArrayList<>();
    private static final Map<String, String> DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS = new HashMap<>();
    private int numberOfSpaces = 1;
    private boolean isChildOfFirstParentNode = true;
    @Getter
    private List<String> identifiers;

    static {
        DL_TO_D_REAL_VALUES_MAPPING.put("!", "not");
        DL_TO_D_REAL_VALUES_MAPPING.put("&&", "and");
        DL_TO_D_REAL_VALUES_MAPPING.put("||", "or");
        DL_TO_D_REAL_VALUES_MAPPING.put("]", "and");
        DL_TO_D_REAL_VALUES_MAPPING.put("->", "=>");
        DL_TO_D_REAL_VALUES_MAPPING.put("<->", "=");
        DL_TO_D_REAL_VALUES_MAPPING.put("==", "=");
        DL_TO_D_REAL_VALUES_MAPPING.put("<<", "<");
        DL_TO_D_REAL_VALUES_MAPPING.put(">>", ">");
        DL_TO_D_REAL_VALUES_MAPPING.put("<EOF>", "");
        log.info("DlToDRealConverter static mapping for operand conversion initialized with {} entries.", DL_TO_D_REAL_VALUES_MAPPING.size());

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

        DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.put("or", "and");
        DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.put("and", "or");
        DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.put("=>", "and");
        log.info("DlToDRealConverter static mapping for logical operand conversion initialized with {} entries.", DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.size());
    }

    public DlToDRealConverter() {
        this.identifiers = new ArrayList<>();
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
            if (node.getValue().matches(Constants.DL_IDENTIFIERS_REGEX))
                this.identifiers.add(node.getValue());
        } else if(isChildOfFirstParentNode || (childNodes.size() == 4 && childNodes.get(0).getValue().equals(Constants.NOT_FOR_D_REAL))) {
            this.convertToDRealOutputForNotOperand(dRealOutputBuilder, childNodes);
        } else if (childNodes.size() == 3 && DL_OPERATORS_WITH_TWO_OPERANDS.contains(childNodes.get(1).getValue()))
            this.convertToDRealOutputForTwoOperands(dRealOutputBuilder, childNodes);
        else if (childNodes.size() == 3 && Constants.IMPLICATION_OPERATOR_FOR_D_REAL.equals(childNodes.get(1).getValue()))
            this.convertToDRealOutputForImplicationOperand(dRealOutputBuilder, childNodes);
        else if (childNodes.size() == 4 && childNodes.get(0).getValue().equals(Constants.DL_BOX_MODALITY_OPENING_BRACKET)) {
            dRealOutputBuilder.append(this.convertToDRealOutput(this.convertToDRealAstNodesForBoxModalityOperator(node)));
        } else if (childNodes.size() == 4 && childNodes.get(0).getValue().equals(Constants.ANGULAR_MODALITY_OPENING_BRACKET_FOR_D_REAL))
            this.convertToDRealAstNodesForAngularModalityOperator(dRealOutputBuilder, node);
        else if (childNodes.size() == 3 && childNodes.get(0).getValue().equals(Constants.ANGULAR_MODALITY_OPENING_BRACKET_FOR_D_REAL))
            this.convertToDRealOutputForAngularModalityOperator(dRealOutputBuilder, childNodes);
        else {
            for (AstNode childNode : node.getChildren())
                dRealOutputBuilder.append(this.convertToDRealOutput(childNode));
        }
        return dRealOutputBuilder;
    }

    private void convertToDRealOutputForTwoOperands(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        if (childNodes.get(1).getValue().equals(Constants.DL_NOT_EQUAL_OPERATOR)) {
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

    private void convertToDRealOutputForImplicationOperand(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputBuilder.append("(").append(Constants.OR_FOR_D_REAL);
        dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputBuilder.append("(").append(Constants.NOT_FOR_D_REAL);
        int indexOfSecondOpenBracket = dRealOutputBuilder.length();
        dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.getFirst())).append(" ");
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf("\n", indexOfSecondOpenBracket) != -1)
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(")");
        dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(2)));
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf("\n", 1) != -1)
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(")");
    }

    private void convertToDRealOutputForNotOperand(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        AstNode node;
        if(this.isChildOfFirstParentNode) {
            node = childNodes.getFirst();
            this.isChildOfFirstParentNode = false;
        } else
            node = childNodes.get(2);
        List<AstNode> childNodesOfOperator = node.getChildren();
        if (childNodesOfOperator.size() == 3 && DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.containsKey(childNodesOfOperator.get(1).getValue())) {
            if(childNodesOfOperator.get(1).getValue().equals(Constants.IMPLICATION_OPERATOR_FOR_D_REAL)) {
                childNodesOfOperator.get(1).setValue(DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.get(childNodesOfOperator.get(1).getValue()));
                childNodesOfOperator.set(2, formNewAstNode(childNodesOfOperator.get(2)));
            } else {
                childNodesOfOperator.set(0, formNewAstNode(childNodesOfOperator.get(0)));
                childNodesOfOperator.get(1).setValue(DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS.get(childNodesOfOperator.get(1).getValue()));
                childNodesOfOperator.set(2, formNewAstNode(childNodesOfOperator.get(2)));
            }
            dRealOutputBuilder.append(this.convertToDRealOutput(node));
        } else if (childNodesOfOperator.size() == 4 && childNodesOfOperator.get(0).getValue().equals(Constants.NOT_FOR_D_REAL))
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodesOfOperator.get(2)));
        else if (childNodesOfOperator.size() == 4 && childNodesOfOperator.getFirst().getValue().equals(Constants.DL_BOX_MODALITY_OPENING_BRACKET)) {
            AstNode childNode = this.convertToDRealAstNodesForBoxModalityOperator(node);
            dRealOutputBuilder.append(this.convertToDRealOutput(childNode));
        } else {
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append("(").append(Constants.NOT_FOR_D_REAL);
            dRealOutputBuilder.append(this.convertToDRealOutput(node));
            this.numberOfSpaces--;
            if (dRealOutputBuilder.indexOf("\n", 1) != -1)
                dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
            dRealOutputBuilder.append(")");
        }
    }

    private void convertToDRealOutputForTernaryOperands(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputBuilder.append("(").append(childNodes.get(0).getValue());
        dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(1)));
    }

    private AstNode convertToDRealAstNodesForBoxModalityOperator(AstNode node) {
        List<AstNode> childNodes = node.getChildren();
        AstNode newNode = new AstNode(Constants.AST_NODE_DL_FORMULA);
        newNode.getChildren().add(new AstNode(Constants.NOT_FOR_D_REAL));
        newNode.getChildren().add(new AstNode(Constants.DL_OPEN_BRACKETS));

        AstNode newChildNode = new AstNode(Constants.PROGRAM_IN_FORMULA_FOR_DL_IN_D_REAL);
        childNodes.getFirst().setValue(Constants.ANGULAR_MODALITY_OPENING_BRACKET_FOR_D_REAL);
        newChildNode.getChildren().add(childNodes.getFirst());
        newChildNode.getChildren().add(childNodes.get(1));
        childNodes.get(2).setValue(Constants.ANGULAR_MODALITY_CLOSING_BRACKET_FOR_D_REAL);
        newChildNode.getChildren().add(childNodes.get(2));
        childNodes.set(0, newChildNode);
        newChildNode = new AstNode(Constants.OR_FOR_D_REAL);
        childNodes.set(1, newChildNode);
        childNodes.remove(2);

        newChildNode = new AstNode(Constants.AST_NODE_DL_FORMULA);
        newNode.getChildren().add(new AstNode(Constants.NOT_FOR_D_REAL));
        newNode.getChildren().add(new AstNode(Constants.DL_OPEN_BRACKETS));
        newNode.getChildren().add(childNodes.getLast());
        newNode.getChildren().add(new AstNode(Constants.DL_CLOSE_BRACKETS));

        childNodes.set(2, newChildNode);
        newNode.getChildren().add(node);
        newNode.getChildren().add(new AstNode(Constants.DL_CLOSE_BRACKETS));
        return newNode;
    }

    private void convertToDRealOutputForAngularModalityOperator(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputBuilder.append(Constants.DL_OPEN_BRACKETS).append(childNodes.getFirst().getValue());
        this.convertToDRealOutputForProgram(dRealOutputBuilder, childNodes.get(1).getChildren());
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf("\n", 1) != -1)
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(childNodes.getLast().getValue()).append(Constants.DL_CLOSE_BRACKETS);
    }

    private void convertToDRealAstNodesForAngularModalityOperator(StringBuilder dRealOutputBuilder, AstNode node) {
        List<AstNode> childNodes = node.getChildren();
        AstNode newNode = new AstNode(Constants.PROGRAM_IN_FORMULA_FOR_DL_IN_D_REAL);
        newNode.getChildren().add(childNodes.getFirst());
        newNode.getChildren().add(childNodes.get(1));
        newNode.getChildren().add(childNodes.get(2));
        childNodes.set(0, newNode);
        newNode = new AstNode(Constants.AND_FOR_D_REAL);
        childNodes.set(1, newNode);
        childNodes.remove(2);
        dRealOutputBuilder.append(this.convertToDRealOutput(node));
    }

    private void convertToDRealOutputForProgram(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        Map<String, Integer> variablesMapping = new HashMap<>();
        if (childNodes.get(1).getValue().equals(":="))
            dRealOutputBuilder.append(this.convertToDRealOutputForProgramAssignments(variablesMapping, childNodes));
        else if (childNodes.get(1).getValue().equals(";")) {
            dRealOutputBuilder.append("(").append("and");
            this.convertToDRealOutputForProgram(dRealOutputBuilder, childNodes.getFirst().getChildren());
            this.convertToDRealOutputForProgram(dRealOutputBuilder, childNodes.get(2).getChildren());
        } else if (childNodes.get(0).getValue().equals(Constants.DL_TERNARY_OPERATOR)) {
            this.convertToDRealOutputForTernaryOperands(dRealOutputBuilder, childNodes);
        }
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf("\n", 1) != -1)
            dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(")");
    }

    private StringBuilder convertToDRealOutputForProgramAssignments(Map<String, Integer> variablesMapping, List<AstNode> childNodes) {
        StringBuilder dRealOutputForAssignment = new StringBuilder();
        Set<String> variablesTransformed = new HashSet<>();
        dRealOutputForAssignment.append(this.transformVariables(childNodes.getFirst(), variablesTransformed, variablesMapping, false));
        dRealOutputForAssignment.append(this.transformVariables(childNodes.get(2), variablesTransformed, variablesMapping, true));
        dRealOutputForAssignment.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputForAssignment.append("(").append("=").append(" ");
        dRealOutputForAssignment.append(childNodes.getFirst().getValue()).append(" ");
        dRealOutputForAssignment.append(childNodes.get(2).getValue()).append(")");
        this.numberOfSpaces--;
        return dRealOutputForAssignment;
    }

    private StringBuilder transformVariables(AstNode node, Set<String> variablesTransformed,
                                             Map<String, Integer> variablesMapping, boolean isRightSide) {
        StringBuilder dRealOutputForVariableTransformation = new StringBuilder();
        if (node.getChildren().isEmpty() && node.getValue().matches(Constants.DL_IDENTIFIERS_REGEX)) {
            String value = node.getValue();
            if (variablesTransformed.contains(value))
                node.setValue(value + variablesMapping.get(value) + "'");
            else {
                if (variablesMapping.containsKey(value))
                    variablesMapping.put(value, variablesMapping.get(value) + 1);
                else
                    variablesMapping.put(value, 1);
                variablesTransformed.add(value);
                String newValue = value + variablesMapping.get(value) + "'";
                node.setValue(value + variablesMapping.get(value) + "'");
                this.identifiers.add(newValue);
            }
            if (isRightSide) {
                dRealOutputForVariableTransformation.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
                this.numberOfSpaces++;
                dRealOutputForVariableTransformation.append("(").append("and");
                dRealOutputForVariableTransformation.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
                this.numberOfSpaces++;
                dRealOutputForVariableTransformation.append("(").append("=").append(" ");
                dRealOutputForVariableTransformation.append(node.getValue()).append(" ");
                dRealOutputForVariableTransformation.append(value).append(")");
                this.numberOfSpaces--;
            }
        } else if (!node.getChildren().isEmpty()) {
            for (int index = 0; index < node.getChildren().size(); index++) {
                dRealOutputForVariableTransformation.append(this.transformVariables(node.getChildren().get(index),
                        variablesTransformed, variablesMapping, isRightSide));
            }
        }
        return dRealOutputForVariableTransformation;
    }

    private AstNode formNewAstNode(AstNode node) {
        AstNode newNode = new AstNode(Constants.AST_NODE_DL_FORMULA);
        newNode.getChildren().add(new AstNode(Constants.NOT_FOR_D_REAL));
        newNode.getChildren().add(new AstNode(Constants.DL_OPEN_BRACKETS));
        newNode.getChildren().add(node);
        newNode.getChildren().add(new AstNode(Constants.DL_CLOSE_BRACKETS));
        return newNode;
    }

    public String convertDlToDRealAndLoadIdentifiers(AstNode astRoot, Set<String> identifiers) {
        Objects.requireNonNull(astRoot, "Ast root node cannot be null for conversion.");
        log.info("Starting the conversion of AST from DL to dReal format.");

        convertNodeValues(astRoot);
        log.debug("AST node values are converted to dReal values.");

        String outputBuilder = "(assert" + convertToDRealOutput(astRoot) + "\n)";
        log.info("dReal output string is generated.");
        return outputBuilder.trim();
    }
}