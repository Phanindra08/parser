package edu.charlotte.parser.conversions.dl.dreal;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static edu.charlotte.parser.utils.Constants.*;

@Slf4j
@Component
public class DlToDRealConverter {
    private static final Map<String, String> DL_TO_D_REAL_VALUES_MAPPING = new HashMap<>();
    private static final List<String> DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION = new ArrayList<>();
    private static final Map<String, String> DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION = new HashMap<>();
    private final Map<String, Integer> variablesMapping;
    private int numberOfSpaces = 1;
    private boolean isChildOfFirstParentNode = true;
    private final Set<String> identifiers;
    private final StringBuilder differentialEquation;

    static {
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_NOT_OPERATOR, Constants.NOT_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_AND_OPERATOR, Constants.AND_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_OR_OPERATOR, Constants.OR_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_BOX_MODALITY_CLOSING_BRACKET, Constants.AND_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_IMPLICATION_OPERATOR, Constants.IMPLICATION_OPERATOR_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_BI_IMPLICATION_OPERATOR, Constants.EQUAL_OPERATOR_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_EQUAL_OPERATOR, Constants.EQUAL_OPERATOR_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.DL_ASSIGNMENT_OPERATOR, Constants.EQUAL_OPERATOR_FOR_D_REAL);
        DL_TO_D_REAL_VALUES_MAPPING.put(Constants.EOF, Constants.EMPTY_STRING);
        log.info("DlToDRealConverter static map for operand conversion initialized with {} entries.", DL_TO_D_REAL_VALUES_MAPPING.size());

        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.EQUAL_OPERATOR_FOR_D_REAL);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_LESS_THAN_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_LESS_THAN_AND_EQUAL_TO_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_GREATER_THAN_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_GREATER_THAN_AND_EQUAL_TO_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_NOT_EQUAL_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.AND_FOR_D_REAL);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.OR_FOR_D_REAL);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_ADDITION_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_SUBTRACTION_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_MULTIPLICATION_OPERATOR);
        DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.add(Constants.DL_DIVISION_OPERATOR);
        log.info("DlToDRealConverter static operators with two operands list initialized with {} entries.", DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.size());

        DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION.put(Constants.OR_FOR_D_REAL, Constants.AND_FOR_D_REAL);
        DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION.put(Constants.AND_FOR_D_REAL, Constants.OR_FOR_D_REAL);
        DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION.put(Constants.IMPLICATION_OPERATOR_FOR_D_REAL, Constants.AND_FOR_D_REAL);
        log.info("DlToDRealConverter static map for logical operand conversion initialized with {} entries.", DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION.size());
    }

    public DlToDRealConverter() {
        this.identifiers = new HashSet<>();
        this.variablesMapping = new HashMap<>();
        this.differentialEquation = new StringBuilder();
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
            if (this.variablesMapping.containsKey(node.getValue())) {
                dRealOutputBuilder.append(node.getValue()).append(this.variablesMapping.get(node.getValue()));
                log.debug(Constants.LOG_MESSAGE_FOR_APPENDING_NODE_VALUE_TO_D_REAL_OUTPUT, node.getValue() + this.variablesMapping.get(node.getValue()));
            } else {
                dRealOutputBuilder.append(node.getValue());
                log.debug(Constants.LOG_MESSAGE_FOR_APPENDING_NODE_VALUE_TO_D_REAL_OUTPUT, node.getValue());
                if (node.getValue().matches(Constants.DL_IDENTIFIERS_REGEX))
                    this.identifiers.add(node.getValue());
            }
        } else if (isChildOfFirstParentNode || (childNodes.size() == NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.NOT_FOR_D_REAL_LENGTH) &&
                childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.NOT_FOR_D_REAL)).getValue().equals(Constants.NOT_FOR_D_REAL))) {
            this.convertToDRealOutputForNotOperand(dRealOutputBuilder, childNodes);
        } else if (childNodes.size() == NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.DL_OPERATORS_WITH_TWO_OPERANDS_LENGTH) &&
                DL_OPERATORS_WITH_TWO_OPERANDS_AFTER_CONVERSION.contains(childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_OPERATORS_POSITION_WITH_TWO_OPERANDS)).getValue()))
            this.convertToDRealOutputForTwoOperands(dRealOutputBuilder, childNodes);
        else if (childNodes.size() == NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.LOGICAL_OPERATOR_FOR_DL_LENGTH) &&
                Constants.IMPLICATION_OPERATOR_FOR_D_REAL.equals(childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.IMPLICATION_OPERATOR_FOR_D_REAL)).getValue()))
            this.convertToDRealOutputForImplicationOperand(dRealOutputBuilder, childNodes);
        else if (childNodes.size() == NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.DL_DIAMOND_MODALITY_LENGTH) &&
                childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_DIAMOND_MODALITY_OPENING_BRACKET)).getValue().equals(Constants.DL_DIAMOND_MODALITY_OPENING_BRACKET)) {
            AstNode newNode = this.convertToDRealAstNodesForDiamondModalityOperator(node);
            dRealOutputBuilder.append(this.convertToDRealOutput(newNode));
        } else {
            for (AstNode childNode : node.getChildren())
                dRealOutputBuilder.append(this.convertToDRealOutput(childNode));
        }
        return dRealOutputBuilder;
    }

    private void convertToDRealOutputForTwoOperands(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        int operatorPosition = DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_OPERATORS_POSITION_WITH_TWO_OPERANDS);
        if (childNodes.get(operatorPosition).getValue().equals(Constants.DL_NOT_EQUAL_OPERATOR)) {
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(Constants.NOT_FOR_D_REAL);
            int indexOfSecondOpenBracket = dRealOutputBuilder.length() + 1;
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(Constants.EQUAL_OPERATOR_FOR_D_REAL).append(Constants.SPACE);
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(operatorPosition - 1))).append(Constants.SPACE);
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(operatorPosition + 1)));
            this.numberOfSpaces--;
            if (dRealOutputBuilder.indexOf(NEXT_LINE, indexOfSecondOpenBracket) != -1)
                dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
            dRealOutputBuilder.append(D_REAL_CLOSING_BRACKET);
        } else {
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(childNodes.get(operatorPosition).getValue()).append(Constants.SPACE);
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(operatorPosition - 1))).append(Constants.SPACE);
            dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(operatorPosition + 1)));
        }
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf(NEXT_LINE, 1) != -1)
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(D_REAL_CLOSING_BRACKET);
    }

    private void convertToDRealOutputForImplicationOperand(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        int operatorPosition = DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.IMPLICATION_OPERATOR_FOR_D_REAL);
        dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(Constants.OR_FOR_D_REAL);
        dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
        this.numberOfSpaces++;
        dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(Constants.NOT_FOR_D_REAL);
        int indexAfterSecondOpeningBracket = dRealOutputBuilder.length();
        dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(operatorPosition - 1))).append(Constants.SPACE);
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf(NEXT_LINE, indexAfterSecondOpeningBracket) != -1)
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(D_REAL_CLOSING_BRACKET);
        dRealOutputBuilder.append(this.convertToDRealOutput(childNodes.get(operatorPosition + 1)));
        this.numberOfSpaces--;
        if (dRealOutputBuilder.indexOf(NEXT_LINE, 1) != -1)
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
        dRealOutputBuilder.append(D_REAL_CLOSING_BRACKET);
    }

    private void convertToDRealOutputForNotOperand(StringBuilder dRealOutputBuilder, List<AstNode> childNodes) {
        AstNode node;
        if (this.isChildOfFirstParentNode) {
            node = childNodes.getFirst();
            this.isChildOfFirstParentNode = false;
        } else
            node = childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_FORMULA_POSITION_IN_NOT_OPERATOR));
        List<AstNode> childNodesOfOperator = node.getChildren();
        if (childNodesOfOperator.size() == 3 && DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION.containsKey(childNodesOfOperator.get(1).getValue())) {
            if (!childNodesOfOperator.get(1).getValue().equals(Constants.IMPLICATION_OPERATOR_FOR_D_REAL))
                childNodesOfOperator.set(0, formNewAstNode(childNodesOfOperator.get(0)));
            childNodesOfOperator.get(1).setValue(DL_OPERATORS_WITH_NOT_LOGICAL_OPERANDS_AFTER_CONVERSION.get(childNodesOfOperator.get(1).getValue()));
            childNodesOfOperator.set(2, formNewAstNode(childNodesOfOperator.get(2)));
            dRealOutputBuilder.append(this.convertToDRealOutput(node));
        } else if (childNodesOfOperator.size() == 4 && childNodesOfOperator.get(0).getValue().equals(Constants.NOT_FOR_D_REAL)) {
            if (childNodesOfOperator.get(2).getChildren().size() == 4 &&
                    childNodesOfOperator.get(2).getChildren().getFirst().getValue().equals(Constants.DL_BOX_MODALITY_OPENING_BRACKET)) {
                dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
                this.numberOfSpaces++;
                dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(Constants.NOT_FOR_D_REAL);
                dRealOutputBuilder.append(this.convertToDRealOutput(node));
                this.numberOfSpaces--;
                if (dRealOutputBuilder.indexOf(NEXT_LINE, 1) != -1)
                    dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
                dRealOutputBuilder.append(D_REAL_CLOSING_BRACKET);
            } else
                dRealOutputBuilder.append(this.convertToDRealOutput(childNodesOfOperator.get(2)));
        } else if (childNodesOfOperator.size() == 4 && childNodesOfOperator.getFirst().getValue().equals(Constants.DL_BOX_MODALITY_OPENING_BRACKET)) {
            this.convertToDRealAstNodesForBoxModalityOperator(node);
            dRealOutputBuilder.append(this.convertToDRealOutput(node));
        } else {
            dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
            this.numberOfSpaces++;
            dRealOutputBuilder.append(D_REAL_OPENING_BRACKET).append(Constants.NOT_FOR_D_REAL);
            dRealOutputBuilder.append(this.convertToDRealOutput(node));
            this.numberOfSpaces--;
            if (dRealOutputBuilder.indexOf(NEXT_LINE, 1) != -1)
                dRealOutputBuilder.append(NEXT_LINE).append(TAB.repeat(Math.max(0, this.numberOfSpaces)));
            dRealOutputBuilder.append(D_REAL_CLOSING_BRACKET);
        }
    }

    private void convertToDRealAstNodesForBoxModalityOperator(AstNode node) {
        node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_BOX_MODALITY_OPENING_BRACKET)).setValue(Constants.DL_DIAMOND_MODALITY_OPENING_BRACKET);
        node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_BOX_MODALITY_CLOSING_BRACKET)).setValue(Constants.DL_DIAMOND_MODALITY_CLOSING_BRACKET);

        AstNode newNode = new AstNode(Constants.AST_NODE_DL_FORMULA);
        newNode.getChildren().add(new AstNode(Constants.NOT_FOR_D_REAL));
        newNode.getChildren().add(new AstNode(Constants.DL_OPEN_BRACKETS));
        newNode.getChildren().add(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_FORMULA_POSITION_IN_MODALITY_OPERATOR)));
        newNode.getChildren().add(new AstNode(Constants.DL_CLOSE_BRACKETS));
        node.getChildren().set(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_FORMULA_POSITION_IN_MODALITY_OPERATOR), newNode);
    }

    private AstNode convertToDRealAstNodesForDiamondModalityOperator(AstNode node) {
        List<AstNode> programNodes = new ArrayList<>();
        this.convertToDRealOutputForProgram(programNodes, node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_PROGRAM_POSITION_IN_MODALITY_OPERATOR)).getChildren());
        programNodes.add(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_FORMULA_POSITION_IN_MODALITY_OPERATOR)));
        return this.convertNodesToTree(programNodes);
    }

    private AstNode convertNodesToTree(List<AstNode> programNodes) {
        AstNode prevNode = programNodes.getFirst();
        AstNode headNode = programNodes.getFirst();
        for (int index = 0; index < programNodes.size() - 1; index++) {
            AstNode node;
            if (index == 0) {
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
            if (index == programNodes.size() - 2)
                node.getChildren().add(programNodes.get(index + 1));
            prevNode = node;
        }
        return headNode;
    }

    private void convertToDRealOutputForProgram(List<AstNode> programNodes, List<AstNode> childNodes) {
        if (childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.EQUAL_OPERATOR_FOR_D_REAL)).getValue().equals(Constants.EQUAL_OPERATOR_FOR_D_REAL))
            this.convertToDRealOutputForProgramAssignments(programNodes, childNodes);
        else if (childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_SEQUENTIAL_COMPOSITION)).getValue().equals(Constants.DL_SEQUENTIAL_COMPOSITION)) {
            this.convertToDRealOutputForProgram(programNodes, childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_SEQUENTIAL_COMPOSITION_PROGRAM1)).getChildren());
            this.convertToDRealOutputForProgram(programNodes, childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_SEQUENTIAL_COMPOSITION_PROGRAM2)).getChildren());
        } else if (childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_TERNARY_OPERATOR)).getValue().equals(Constants.DL_TERNARY_OPERATOR)) {
            this.convertToDRealOutputForTernaryOperands(childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_TERNARY_OPERATOR_FORMULA)));
            programNodes.add(childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_TERNARY_OPERATOR_FORMULA)));
        } else if (childNodes.size() >= NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.DL_DIFFERENTIAL_EQUATION_LENGTH) &&
                childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_START_POSITION_OF_DIFFERENTIAL_EQUATION_IN_PROGRAM)).getValue().equals(
                        Constants.AST_NODE_DL_DIFFERENTIAL_EQUATION)) {
            programNodes.add(this.createTimeNodeForIntegrationLimit(Constants.DL_GREATER_THAN_OPERATOR, "0.0"));
            programNodes.add(this.createTimeNodeForIntegrationLimit(Constants.DL_LESS_THAN_AND_EQUAL_TO_OPERATOR, "5.0"));
            this.convertToDRealOutputForDifferentialEquation(programNodes, childNodes);
        }
    }

    private void convertToDRealOutputForProgramAssignments(List<AstNode> programNodes, List<AstNode> childNodes) {
        Set<String> variablesTransformed = new HashSet<>();
        this.transformVariables(childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_ASSIGNMENT_OPERATOR_IDENTIFIER)), variablesTransformed, true);
        this.transformVariables(childNodes.get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_ASSIGNMENT_OPERATOR_TERM)), variablesTransformed, false);
        childNodes.removeLast();
        AstNode newNode = new AstNode(Constants.FORMULA_IN_D_REAL, childNodes);
        programNodes.add(newNode);
    }

    private void convertToDRealOutputForTernaryOperands(AstNode node) {
        Set<String> variablesTransformed = new HashSet<>();
        this.convertToDRealOutputForFormula(node, variablesTransformed, true);
    }

    private AstNode createTimeNodeForIntegrationLimit(String operator, String value) {
        AstNode node = new AstNode(Constants.FORMULA_IN_D_REAL);
        node.getChildren().add(new AstNode(Constants.TIME));
        node.getChildren().add(new AstNode(operator));
        node.getChildren().add(new AstNode(value));
        return node;
    }

    private void convertToDRealOutputForFormula(AstNode node, Set<String> variablesTransformed, boolean canTheVariableBeTransformed) {
        if (node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_LOGICAL_OPERATOR_FORMULA1)).getValue().equals(Constants.AST_NODE_DL_FORMULA) ||
                node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_LOGICAL_OPERATOR_FORMULA2)).getValue().equals(Constants.NOT_FOR_D_REAL)) {
            if (node.getChildren().size() == NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.LOGICAL_OPERATOR_FOR_DL_LENGTH)) {
                this.convertToDRealOutputForTernaryOperands(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_LOGICAL_OPERATOR_FORMULA1)));
                this.convertToDRealOutputForTernaryOperands(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_LOGICAL_OPERATOR_FORMULA2)));
            } else if (node.getChildren().size() == NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.get(Constants.NOT_FOR_D_REAL_LENGTH))
                this.convertToDRealOutputForTernaryOperands(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_FORMULA_POSITION_IN_NOT_OPERATOR)));
        } else {
            this.transformVariables(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_COMPARISON_OPERATOR_TERM1)), variablesTransformed, canTheVariableBeTransformed);
            this.transformVariables(node.getChildren().get(DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_COMPARISON_OPERATOR_TERM2)), variablesTransformed, false);
        }
    }

    private void convertToDRealOutputForDifferentialEquation(List<AstNode> programNodes, List<AstNode> childNodes) {
        Set<String> variablesTransformed = new HashSet<>();
        this.convertToDRealOutputForFormula(childNodes.get(childNodes.size() - DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(
                Constants.DL_POSITION_OF_FORMULA_IN_DIFFERENTIAL_EQUATION_PROGRAM_FROM_LAST)), variablesTransformed, false);
        programNodes.add(childNodes.get(childNodes.size() - DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(
                Constants.DL_POSITION_OF_FORMULA_IN_DIFFERENTIAL_EQUATION_PROGRAM_FROM_LAST)));

        int differentialEquationStartPosition = DL_SYNTAX_POSITIONS_AFTER_CONVERSION.get(Constants.DL_START_POSITION_OF_DIFFERENTIAL_EQUATION_IN_PROGRAM);
        String differentialEquationVariable = childNodes.get(1).getValue().substring(0, childNodes.get(1).getValue().length() - 1);
        AstNode node = new AstNode(Constants.PROGRAM_IN_D_REAL);
        AstNode firstChildNode = new AstNode(differentialEquationVariable);
        this.transformVariables(firstChildNode, variablesTransformed, true);
        firstChildNode.setValue("[" + firstChildNode.getValue() + "]");
        node.getChildren().add(firstChildNode);
        node.getChildren().add(new AstNode(Constants.EQUAL_OPERATOR_FOR_D_REAL));
        String integrationValue = "(integral 0. " + Constants.TIME +
                " [" + differentialEquationVariable +
                (this.variablesMapping.get(differentialEquationVariable) - 1) + "] " + Constants.DIFFERENTIAL_EQUATION + D_REAL_CLOSING_BRACKET;
        node.getChildren().add(new AstNode(integrationValue));
        programNodes.add(node);

        this.transformVariables(childNodes.get(3), variablesTransformed, false);
        differentialEquation.append("(define-ode ").append(Constants.DIFFERENTIAL_EQUATION)
                .append(" (\n\t(= d/dt[").append(differentialEquationVariable).append("] ")
                .append(childNodes.get(3).getValue()).append(")\n))\n\n");
        this.identifiers.add(differentialEquationVariable);
    }

    private StringBuilder transformVariables(AstNode node, Set<String> variablesTransformed,
                                             boolean isLeftSide) {
        StringBuilder dRealOutputForVariableTransformation = new StringBuilder();
        if (node.getChildren().isEmpty() && node.getValue().matches(Constants.DL_IDENTIFIERS_REGEX)) {
            String value = node.getValue();
            if (variablesTransformed.contains(value)) {
                if (isLeftSide)
                    node.setValue(value + this.variablesMapping.get(value));
                else {
                    node.setValue(value + (this.variablesMapping.get(value) - 1));
                    this.identifiers.add(value + (this.variablesMapping.get(value) - 1));
                }
            } else {
                if (!this.variablesMapping.containsKey(value))
                    variablesMapping.put(value, 0);
                if (isLeftSide) {
                    variablesMapping.put(value, variablesMapping.get(value) + 1);
                    variablesTransformed.add(value);
                    this.identifiers.add(value + variablesMapping.get(value));
                }
                node.setValue(value + variablesMapping.get(value));
            }
        } else if (!node.getChildren().isEmpty()) {
            for (int index = 0; index < node.getChildren().size(); index++) {
                dRealOutputForVariableTransformation.append(this.transformVariables(node.getChildren().get(index),
                        variablesTransformed, isLeftSide));
            }
        }
        return dRealOutputForVariableTransformation;
        // Check the else-if part in the future, I think it does not have any significance
    }

    private AstNode formNewAstNode(AstNode node) {
        AstNode newNode = new AstNode(Constants.AST_NODE_DL_FORMULA);
        newNode.getChildren().add(new AstNode(Constants.NOT_FOR_D_REAL));
        newNode.getChildren().add(new AstNode(Constants.DL_OPEN_BRACKETS));
        newNode.getChildren().add(node);
        newNode.getChildren().add(new AstNode(Constants.DL_CLOSE_BRACKETS));
        return newNode;
    }

    public String convertDlToDReal(AstNode astRoot) {
        Objects.requireNonNull(astRoot, "Ast root node cannot be null for conversion.");
        log.info("Starting the conversion of AST from DL to dReal format.");

        convertNodeValues(astRoot);
        log.debug("AST node values are converted to dReal values.");

        String outputBuilder = "(assert" + convertToDRealOutput(astRoot) + "\n)";
        log.info("dReal output string is generated.");
        return this.differentialEquation.toString() + outputBuilder.trim();
    }

    public List<String> getIdentifiers() {
        return new ArrayList<>(identifiers);
    }
}