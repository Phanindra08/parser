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
        else if (childNodes.size() == 4 && childNodes.get(0).getValue().equals(Constants.ANGULAR_MODALITY_OPENING_BRACKET_FOR_D_REAL)) {
            AstNode newNode = this.convertToDRealAstNodesForAngularModalityOperator(node);
            dRealOutputBuilder.append(this.convertToDRealOutput(newNode));
        } else {
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
        } else if (childNodesOfOperator.size() == 4 && childNodesOfOperator.get(0).getValue().equals(Constants.NOT_FOR_D_REAL)) {
            if(childNodesOfOperator.get(2).getChildren().size() == 4 &&
                    childNodesOfOperator.get(2).getChildren().getFirst().getValue().equals(Constants.DL_BOX_MODALITY_OPENING_BRACKET) ) {
                dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
                this.numberOfSpaces++;
                dRealOutputBuilder.append("(").append(Constants.NOT_FOR_D_REAL);
                dRealOutputBuilder.append(this.convertToDRealOutput(node));
                this.numberOfSpaces--;
                if (dRealOutputBuilder.indexOf("\n", 1) != -1)
                    dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
                dRealOutputBuilder.append(")");
            } else
                dRealOutputBuilder.append(this.convertToDRealOutput(childNodesOfOperator.get(2)));
        } else if (childNodesOfOperator.size() == 4 && childNodesOfOperator.getFirst().getValue().equals(Constants.DL_BOX_MODALITY_OPENING_BRACKET)) {
            this.convertToDRealAstNodesForBoxModalityOperator(node);
            dRealOutputBuilder.append(this.convertToDRealOutput(node));
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

//    private void convertToDRealOutputForTernaryOperands(List<AstNode> programNodes, List<AstNode> childNodes) {
//        dRealOutputBuilder.append("\n").append("\t".repeat(Math.max(0, this.numberOfSpaces)));
//        this.numberOfSpaces++;
//        dRealOutputBuilder.append("(").append(childNodes.get(0).getValue());
//        dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(1)));
//    }

    private void convertToDRealAstNodesForBoxModalityOperator(AstNode node) {
        node.getChildren().getFirst().setValue(Constants.ANGULAR_MODALITY_OPENING_BRACKET_FOR_D_REAL);
        node.getChildren().get(2).setValue(Constants.ANGULAR_MODALITY_CLOSING_BRACKET_FOR_D_REAL);

        AstNode newNode = new AstNode(Constants.AST_NODE_DL_FORMULA);
        newNode.getChildren().add(new AstNode(Constants.NOT_FOR_D_REAL));
        newNode.getChildren().add(new AstNode(Constants.DL_OPEN_BRACKETS));
        newNode.getChildren().add(node.getChildren().getLast());
        newNode.getChildren().add(new AstNode(Constants.DL_CLOSE_BRACKETS));
        node.getChildren().removeLast();
        node.getChildren().add(newNode);
    }

    private AstNode convertToDRealAstNodesForAngularModalityOperator(AstNode node) {
        List<AstNode> programNodes = new ArrayList<>();
        this.convertToDRealOutputForProgram(programNodes, node.getChildren().get(1).getChildren());
        programNodes.add(node.getChildren().getLast());
        return this.convertNodesToTree(programNodes);
    }

    private AstNode convertNodesToTree(List<AstNode> programNodes) {
        AstNode prevNode = programNodes.getFirst();
        AstNode headNode = programNodes.getFirst();
        for(int index = 0; index < programNodes.size() - 1; index++) {
            AstNode node;
            if(index == 0) {
                node = new AstNode(Constants.PROGRAM_IN_D_REAL);
                node.getChildren().add(programNodes.get(index));
                node.getChildren().add(new AstNode(Constants.AND_FOR_D_REAL));
                headNode = node;
            } else {
                node = new AstNode(Constants.PROGRAM_IN_D_REAL);
                node.getChildren().add(programNodes.get(index));
                node.getChildren().add(new AstNode(Constants.AND_FOR_D_REAL));
                prevNode.getChildren().add(node);
            }
            if(index == programNodes.size() - 2)
                node.getChildren().add(programNodes.get(index + 1));
            prevNode = node;
        }
        return headNode;
    }

    private void convertToDRealOutputForProgram(List<AstNode> programNodes, List<AstNode> childNodes) {
        Map<String, Integer> variablesMapping = new HashMap<>();
        if (childNodes.get(1).getValue().equals(":="))
            this.convertToDRealOutputForProgramAssignments(programNodes, childNodes, variablesMapping);
        else if (childNodes.get(1).getValue().equals(";")) {
            this.convertToDRealOutputForProgram(programNodes, childNodes.getFirst().getChildren());
            this.convertToDRealOutputForProgram(programNodes, childNodes.get(2).getChildren());
        }
//        else if (childNodes.get(0).getValue().equals(Constants.DL_TERNARY_OPERATOR)) {
//            this.convertToDRealOutputForTernaryOperands(programNodes, childNodes);
//        }
    }

    private void convertToDRealOutputForProgramAssignments(List<AstNode> programNodes, List<AstNode> childNodes, Map<String, Integer> variablesMapping) {
        Set<String> variablesTransformed = new HashSet<>();
        this.transformVariables(programNodes, childNodes.getFirst(), variablesTransformed, variablesMapping, false);
        if(childNodes.get(1).getValue().equals(":="))
            childNodes.get(1).setValue("=");
        this.transformVariables(programNodes, childNodes.get(2), variablesTransformed, variablesMapping, true);
        childNodes.removeLast();
        AstNode newNode = new AstNode(Constants.FORMULA_IN_D_REAL, childNodes);
        programNodes.add(newNode);
    }

    private StringBuilder transformVariables(List<AstNode> programNodes, AstNode node, Set<String> variablesTransformed,
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
                this.identifiers.add(newValue.substring(0, newValue.length() - 1));
            }
            if (isRightSide) {
                AstNode newNode = new AstNode(Constants.FORMULA_IN_D_REAL);
                newNode.getChildren().add(new AstNode(node.getValue()));
                newNode.getChildren().add(new AstNode(Constants.D_REAL_ASSIGNMENT_OPERATOR));
                newNode.getChildren().add(new AstNode(node.getValue().substring(0, node.getValue().length() - 1)));
                programNodes.add(newNode);
            }
        } else if (!node.getChildren().isEmpty()) {
            for (int index = 0; index < node.getChildren().size(); index++) {
                dRealOutputForVariableTransformation.append(this.transformVariables(programNodes, node.getChildren().get(index),
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