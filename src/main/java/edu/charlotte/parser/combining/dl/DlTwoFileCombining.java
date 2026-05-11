package edu.charlotte.parser.combining.dl;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.exceptions.InvalidInputException;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class DlTwoFileCombining {

    public DlTwoFileCombining() {
        log.info("DlTwoFileCombining instance is created.");
    }

    private AstNode combineNodeValues(AstNode astRootForInput1, AstNode astRootForInput2,
                                      float constantValue, Map<String, String> identifiersConversionMappingForInput2) {
        if (astRootForInput1 == null || astRootForInput2 == null) {
            log.debug("Node is null, skipping value conversion.");
            return null;
        }

        try {
            AstNode root = new AstNode(Constants.AST_NODE_DL_PROGRAM);
            AstNode formula1 = new AstNode(Constants.AST_NODE_DL_FORMULA);
            this.transformVariablesInAstTree(astRootForInput2.getChildren().getFirst(), identifiersConversionMappingForInput2);

            List<AstNode> childListForRoot = new ArrayList<>();
            childListForRoot.add(formula1);
            childListForRoot.add(astRootForInput1.getChildren().getLast());
            root.addChildren(childListForRoot);

            List<AstNode> childListForFormula1 = new ArrayList<>();
            AstNode formulaNodeForInput1 = astRootForInput1.getChildren().getFirst();

            AstNode formula2 = new AstNode(Constants.AST_NODE_DL_FORMULA);
            List<AstNode> childListForFormula2 = new ArrayList<>();
            childListForFormula2.add(formulaNodeForInput1.getChildren().getFirst());
            childListForFormula2.add(new AstNode(Constants.DL_AND_OPERATOR));
            childListForFormula2.add(astRootForInput2.getChildren().getFirst().getChildren().getFirst());
            formula2.addChildren(childListForFormula2);

            childListForFormula1.add(formula2);
            childListForFormula1.add(formulaNodeForInput1.getChildren().get(1));
            AstNode formula3 = new AstNode(Constants.AST_NODE_DL_FORMULA);
            List<AstNode> childListForFormula3 = new ArrayList<>();

            AstNode formulaNode2ForInput1 = formulaNodeForInput1.getChildren().getLast();
            AstNode formulaNode2ForInput2 = astRootForInput2.getChildren().getFirst().getChildren().getLast();
            childListForFormula3.add(formulaNode2ForInput1.getChildren().getFirst());

            AstNode programNodeForInput1 = formulaNode2ForInput1.getChildren().get(1);
            AstNode programNodeForInput2 = formulaNode2ForInput2.getChildren().get(1);

            if (!programNodeForInput1.getValue().equals(Constants.AST_NODE_DL_PROGRAM_CONTEXT))
                throw new InvalidInputException("The Input 1 provided does not contain DL program.");

            if (!programNodeForInput2.getValue().equals(Constants.AST_NODE_DL_PROGRAM_CONTEXT))
                throw new InvalidInputException("The Input 2 provided does not contain DL program.");

            if (!programNodeForInput1.getChildren().get(1).getValue().equals(Constants.AST_NODE_DL_DIFFERENTIAL_EQUATION))
                throw new InvalidInputException("The Input 1 provided does not contain any Differential Equation in DL program.");

            if (!programNodeForInput2.getChildren().get(1).getValue().equals(Constants.AST_NODE_DL_DIFFERENTIAL_EQUATION))
                throw new InvalidInputException("The Input 2 provided does not contain any Differential Equation in DL program.");

            AstNode programNode = new AstNode(Constants.AST_NODE_DL_PROGRAM_CONTEXT);
            List<AstNode> childListForProgramNode = new ArrayList<>();
            childListForProgramNode.add(new AstNode(Constants.DL_OPEN_CURLY_BRACKETS));
            childListForProgramNode.add(programNodeForInput1.getChildren().get(1));
            childListForProgramNode.add(new AstNode(Constants.DL_COMMA));
            childListForProgramNode.add(programNodeForInput2.getChildren().get(1));

            AstNode newBinaryExpressionForInput2 = new AstNode(Constants.AST_NODE_DL_BINARY_EXPRESSION);
            newBinaryExpressionForInput2.getChildren().add(new AstNode(Float.toString(constantValue)));
            newBinaryExpressionForInput2.getChildren().add(new AstNode(Constants.DL_MULTIPLICATION_OPERATOR));
            AstNode astRootForInput2TermWithParentheses = new AstNode(Constants.AST_NODE_DL_TERM_WITH_PARENTHESES);
            newBinaryExpressionForInput2.getChildren().add(astRootForInput2TermWithParentheses);
            List<AstNode> childrenForTermWithParenthesesForInput2 = astRootForInput2TermWithParentheses.getChildren();
            childrenForTermWithParenthesesForInput2.add(new AstNode(Constants.DL_OPEN_BRACKETS));
            childrenForTermWithParenthesesForInput2.add(programNodeForInput2.getChildren().get(1).getChildren().getLast());
            childrenForTermWithParenthesesForInput2.add(new AstNode(Constants.DL_CLOSE_BRACKETS));
            programNodeForInput2.getChildren().get(1).getChildren().set(2, newBinaryExpressionForInput2);

            AstNode formula4 = new AstNode(Constants.AST_NODE_DL_FORMULA);
            List<AstNode> childListForFormula4 = new ArrayList<>();
            childListForFormula4.add(programNodeForInput1.getChildren().get(3));
            childListForFormula4.add(new AstNode(Constants.DL_AND_OPERATOR));
            childListForFormula4.add(programNodeForInput2.getChildren().get(3));
            formula4.addChildren(childListForFormula4);

            childListForProgramNode.add(new AstNode(Constants.DL_AND_OPERATOR));
            childListForProgramNode.add(formula4);
            childListForProgramNode.add(new AstNode(Constants.DL_CLOSE_CURLY_BRACKETS));
            programNode.addChildren(childListForProgramNode);

            childListForFormula3.add(programNode);
            childListForFormula3.add(formulaNode2ForInput1.getChildren().get(2));

            AstNode formula5 = new AstNode(Constants.AST_NODE_DL_FORMULA);
            List<AstNode> childListForFormula5 = new ArrayList<>();
            childListForFormula5.add(formulaNode2ForInput1.getChildren().getLast());
            childListForFormula5.add(new AstNode(Constants.DL_AND_OPERATOR));
            childListForFormula5.add(formulaNode2ForInput2.getChildren().getLast());
            formula5.addChildren(childListForFormula5);

            childListForFormula3.add(formula5);
            formula3.addChildren(childListForFormula3);
            childListForFormula1.add(formula3);
            formula1.addChildren(childListForFormula1);
            return root;
        } catch (InvalidInputException e) {
            log.error("{}", e.getMessage());
            throw new InvalidInputException(e.getMessage(), e);
        }
    }

    private void transformVariablesInAstTree(AstNode node, Map<String, String> identifiersConversionMappingForInput2) {
        String value;
        if (!identifiersConversionMappingForInput2.isEmpty()) {
            if (node.getChildren().isEmpty() && node.getValue() != null && !node.getValue().trim().isEmpty() && !node.getValue().equalsIgnoreCase(Constants.EOF)) {
                value = node.getValue().indexOf(Constants.DASH) == node.getValue().length() - 1 ? node.getValue().substring(0, node.getValue().length() - 1) : node.getValue();
                value = identifiersConversionMappingForInput2.getOrDefault(value, value);
                if (node.getValue().indexOf(Constants.DASH) == node.getValue().length() - 1)
                    value = value + Constants.DASH;
                if (!node.getValue().equals(value))
                    log.debug("Converting the identifier: '{}' to '{}'.", node.getValue(), identifiersConversionMappingForInput2.get(node.getValue()));
                node.setValue(value);
            } else {
                for (AstNode childNode : node.getChildren())
                    this.transformVariablesInAstTree(childNode, identifiersConversionMappingForInput2);
            }
        }
    }

    private void appendCombinedDlOutput(AstNode node, StringBuilder combinedDLProgram) {
        if (node == null) {
            log.debug("Attempted to append a null AstNode to  output.");
            return;
        }

        if (node.getChildren().isEmpty() && node.getValue() != null && !node.getValue().trim().isEmpty() && !node.getValue().equalsIgnoreCase(Constants.EOF)) {
            combinedDLProgram.append(node.getValue()).append(" ");
            log.debug("Appended the node value '{}' to the combined output.", node.getValue());
        } else {
            if (node.getChildren().size() == Constants.DL_NOT_FORMULA_SIZE && node.getChildren().getFirst().getValue().equals(Constants.DL_NOT_OPERATOR)) {
                node.getChildren().get(1).setValue("");
                node.getChildren().get(Constants.DL_NOT_FORMULA_SIZE - 1).setValue("");
            }
            for (AstNode childNode : node.getChildren())
                this.appendCombinedDlOutput(childNode, combinedDLProgram);
        }
    }

    public AstNode combiningTwoDlFiles(AstNode astRootForInput1, AstNode astRootForInput2,
                                       float constantValue, Map<String, String> identifiersConversionMappingForInput2) {
        Objects.requireNonNull(astRootForInput1, "Ast root cannot be null while combining two DL inputs.");
        Objects.requireNonNull(astRootForInput2, "Ast root cannot be null while combining two DL inputs.");
        log.info("Starting to combine two DL inputs.");

        AstNode astRoot = combineNodeValues(astRootForInput1, astRootForInput2, constantValue, identifiersConversionMappingForInput2);
        log.debug("DL AST node values are combined into one DL AST.");
        return astRoot;
    }
}
