package edu.charlotte.parser.listeners.ast;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.relational_dynamic_logic.RelationalDynamicLogicBaseListener;
import edu.charlotte.parser.relational_dynamic_logic.RelationalDynamicLogicParser;
import edu.charlotte.parser.utils.AstListenerUtils;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RelDlAstListener extends RelationalDynamicLogicBaseListener {
    private final Stack<AstNode> stack;
    private final Map<Character, Set<String>> identifiers;
    private final boolean hasKeYMaeraXConversion;
    private char programConsidered;

    public RelDlAstListener(boolean hasKeYMaeraXConversion) {
        this.stack = new Stack<>();
        this.identifiers = new HashMap<>();
        this.identifiers.put(Constants.PROGRAM_CONSIDERED_L, new HashSet<>());
        this.identifiers.put(Constants.PROGRAM_CONSIDERED_R, new HashSet<>());
        this.identifiers.put(Constants.PROGRAM_CONSIDERED_G, new HashSet<>());
        this.programConsidered = Constants.PROGRAM_CONSIDERED_G;
        this.hasKeYMaeraXConversion = hasKeYMaeraXConversion;
        log.debug("RelDlAstListener is initialized with the default program considered is '{}'. KeYMaeraX conversion enabled: {}.",
                this.programConsidered, this.hasKeYMaeraXConversion);
    }

    // Relation DL Program (root of the file)
    @Override
    public void enterRelDlProgram(RelationalDynamicLogicParser.RelDlProgramContext ctx) {
        log.debug("Entering Relational DL-Program rule: {}", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_REL_DL_PROGRAM));
    }

    @Override
    public void exitRelDlProgram(RelationalDynamicLogicParser.RelDlProgramContext ctx) {
        log.debug("Exiting Relational DL-Program rule: {}", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_REL_DL_PROGRAM, ctx.getText(), stack);
    }

    @Override
    public void enterRelProgram(RelationalDynamicLogicParser.RelProgramContext ctx) {
        log.debug("Entering Relational program context rule: {}", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_REL_DL_PROGRAM_CONTEXT));
    }

    @Override
    public void exitRelProgram(RelationalDynamicLogicParser.RelProgramContext ctx) {
        log.debug("Exiting Relational program context rule: {}", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        if(hasKeYMaeraXConversion) {
            if(ctx.REL_DL_TERNARY_OPERATOR() != null) {
                log.info("The Relational program context contains a ternary operator. " +
                        "Adding the ';' symbol as a child node to the AST Node List for Converting to KeyMaeraX.");
                childNodes.add(new AstNode(";"));
            } else if(ctx.REL_DL_ASSIGNMENT_OPERATOR() != null) {
                log.info("The Relational program context contains a assignment operator. " +
                        "Expanding the relational assignment operator into equivalent DL assignment nodes for Converting to KeYMaeraX.");
                childNodes = expandRelationalAssignmentOperator(childNodes);
                log.debug("Expanded the relational assignment operator {}.", ctx.getText());
            }
        }
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_REL_DL_PROGRAM_CONTEXT, ctx.getText(), stack);
    }

    @Override
    public void enterRelFormula(RelationalDynamicLogicParser.RelFormulaContext ctx) {
        log.debug("Entering Relational formula rule: {}", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_REL_DL_FORMULA));
    }

    @Override
    public void exitRelFormula(RelationalDynamicLogicParser.RelFormulaContext ctx) {
        log.debug("Exiting Relational formula rule: {}", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_REL_DL_FORMULA, ctx.getText(), stack);
    }

    @Override
    public void enterRelTerm(RelationalDynamicLogicParser.RelTermContext ctx) {
        log.debug("Entering Relational term rule: {}", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_REL_DL_TERM));
        if(ctx.PROGRAM_CONSIDERED() != null) {
            if (ctx.PROGRAM_CONSIDERED().getText().equals(Constants.LEFT_PROGRAM))
                this.programConsidered = Constants.PROGRAM_CONSIDERED_L;
            else if (ctx.PROGRAM_CONSIDERED().getText().equals(Constants.RIGHT_PROGRAM))
                this.programConsidered = Constants.PROGRAM_CONSIDERED_R;
            else
                this.programConsidered = Constants.PROGRAM_CONSIDERED_G;
            log.info("Program considered is set to '{}' for current RelTerm context.", this.programConsidered);
        } else
            log.error("No specific program considered token found in the RelTerm context.");
    }

    @Override
    public void exitRelTerm(RelationalDynamicLogicParser.RelTermContext ctx) {
        log.debug("Exiting Relational term rule: {}", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_REL_DL_TERM, ctx.getText(), stack);

        this.programConsidered = Constants.PROGRAM_CONSIDERED_G;
        log.info("Program considered is reset to the default value '{}' after exiting RelTerm.", this.programConsidered);
    }

    // DL Formula Handling
    @Override
    public void enterFormula(RelationalDynamicLogicParser.FormulaContext ctx) {
        log.debug("Entering DL formula rule '{}' within Relational DL.", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_DL_FORMULA));
    }

    @Override
    public void exitFormula(RelationalDynamicLogicParser.FormulaContext ctx) {
        log.debug("Exiting DL formula rule '{}' within Relational DL.", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_FORMULA, ctx.getText(), stack);
    }

    // DL Program Handling
    @Override
    public void enterProgram(RelationalDynamicLogicParser.ProgramContext ctx) {
        log.debug("Entering DL program rule '{}' within Relational DL.", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_DL_PROGRAM_CONTEXT));
        if(ctx.IDENTIFIER() != null) {
            this.addIdentifierToSet(ctx.IDENTIFIER().getText());
            log.debug("Found identifier '{}' in the nested program context.", ctx.IDENTIFIER().getText());
        }
        if(ctx.IDENTIFIER_PRIME() != null) {
            String identifierPrime = ctx.IDENTIFIER_PRIME().getText();
            if (identifierPrime.endsWith("'")) {
                String identifier = identifierPrime.substring(0, identifierPrime.length() - 1);
                this.addIdentifierToSet(identifier);
                log.debug("Found primed identifier '{}' in the nested program context.", identifierPrime);
            } else {
                log.warn("Identifier prime '{}' does not end with a prime(') character as expected. Adding full text to the identifiers array.", identifierPrime);
                this.addIdentifierToSet(identifierPrime);
            }
        }
    }

    @Override
    public void exitProgram(RelationalDynamicLogicParser.ProgramContext ctx) {
        log.debug("Exiting DL program rule '{}' within Relational DL.", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_PROGRAM_CONTEXT, ctx.getText(), stack);
    }

    @Override
    public void enterAssignmentIdentifier(RelationalDynamicLogicParser.AssignmentIdentifierContext ctx) {
        log.debug("Entering Assignment Identifier rule '{}' within Relational DL.", ctx.getText());
        if(ctx.IDENTIFIER() != null) {
            String identifier = ctx.IDENTIFIER().getText();
            this.addIdentifierToSet(identifier);
            log.debug("Found the identifier '{}' in the Assignment Identifier rule.", identifier);
        }
    }

    @Override
    public void enterBinaryExpr(RelationalDynamicLogicParser.BinaryExprContext ctx) {
        log.debug("Entering Binary Expression rule '{}' within Relational DL.", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_DL_BINARY_EXPRESSION));
    }

    @Override
    public void exitBinaryExpr(RelationalDynamicLogicParser.BinaryExprContext ctx) {
        log.debug("Exiting Binary Expression rule '{}' within Relational DL.", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_BINARY_EXPRESSION, ctx.getText(), stack);
    }

    @Override
    public void enterParenthesesTerm(RelationalDynamicLogicParser.ParenthesesTermContext ctx) {
        log.debug("Entering Parentheses Term rule '{}' within Relational DL.", ctx.getText());
        stack.push(new AstNode(Constants.AST_NODE_DL_TERM_WITH_PARENTHESES));
    }

    @Override
    public void exitParenthesesTerm(RelationalDynamicLogicParser.ParenthesesTermContext ctx) {
        log.debug("Exiting Parentheses Term rule: {}", ctx.getText());
        List<AstNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_TERM_WITH_PARENTHESES, ctx.getText(), stack);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        log.debug("Visiting terminal '{}' within Relational DL.", node.getText());
        stack.push(new AstNode(node.getText()));
    }

    private void addIdentifierToSet(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            log.warn("Attempted to add a null or empty identifier to the identifiers set.");
            return;
        }

        Set<String> targetSet = identifiers.computeIfAbsent(this.programConsidered, k -> {
            log.error("Identifier set for programConsidered '{}' was null. Initializing a new set.", this.programConsidered);
            return new HashSet<>();
        });
        targetSet.add(identifier);
        log.debug("Added identifier '{}' to the program considered '{}'.", identifier, this.programConsidered);
    }

    private List<AstNode> expandRelationalAssignmentOperator(List<AstNode> childNodes) {
        List<AstNode> newChildNodes = new ArrayList<>();
        if (childNodes.size() != 3) {
            log.error("Invalid number of child nodes for relational assignment operator. Expected 3 child nodes, but got {}." +
                    "Cannot perform KeYMaeraX conversion for this Relational DL Program.", childNodes.size());
            return childNodes;
        }

        AstNode leftTerm = childNodes.get(0);
        AstNode operator = childNodes.get(1);
        AstNode rightTerm = childNodes.get(2);

        if (!operator.getValue().equals(Constants.REL_DL_ASSIGNMENT_OPERATOR)) {
            log.error("Expected relational assignment operator ({}) at index 1, but found '{}' in the child nodes. Cannot expand the relational assignment operator.",
                    Constants.REL_DL_ASSIGNMENT_OPERATOR, operator.getValue());
            return childNodes;
        }

        newChildNodes.add(new AstNode(Constants.REL_DL_OPEN_BRACKETS));
        newChildNodes.add(createAssignmentProgramNode(leftTerm, rightTerm));
        newChildNodes.add(new AstNode(Constants.REL_DL_COMMA));
        newChildNodes.add(createAssignmentProgramNode(leftTerm, rightTerm));
        newChildNodes.add(new AstNode(Constants.REL_DL_CLOSE_BRACKETS));
        return newChildNodes;
    }

    private List<AstNode> createNewChildNodes(List<AstNode> childNodes) {
        log.debug("Creating new child nodes from the existing child nodes.");
        return childNodes.stream()
                .map(child -> new AstNode(child.getValue(), createNewChildNodes(child.getChildren())))
                .collect(Collectors.toList());
    }

    private AstNode createAssignmentProgramNode(AstNode leftTerm, AstNode rightTerm) {
        log.debug("Creating assignment program nodes for the left term '{}' and the right term '{}' as part of expansion for the " +
                "Relational assignment operator.", leftTerm.getValue(), rightTerm.getValue());
        AstNode assignmentProgram = new AstNode(Constants.AST_NODE_DL_PROGRAM_CONTEXT);
        assignmentProgram.addChildren(Arrays.asList(
                new AstNode(leftTerm.getValue()),
                new AstNode(Constants.DL_ASSIGNMENT_OPERATOR),
                new AstNode(rightTerm.getValue(), createNewChildNodes(rightTerm.getChildren())),
                new AstNode(Constants.DL_SEMI_COLON)
        ));
        return assignmentProgram;
    }

    // Return the final Ast root node
    public AstNode getAst() {
        if(stack.size() > 1)
            log.warn("Stack contains more than one element after AST generation. There might be a possible issue in listener logic. " +
                    "Stack size is: {} and the contents are: {}", stack.size(), stack);
        return stack.isEmpty() ? null : stack.pop();
    }

    // Return the final Set of Identifiers
    public Map<Character, Set<String>> getIdentifiers() {
        Map<Character, Set<String>> unmodifiableMap = new HashMap<>();
        for (Map.Entry<Character, Set<String>> entry : this.identifiers.entrySet()) {
            unmodifiableMap.put(entry.getKey(), Collections.unmodifiableSet(Objects.requireNonNullElse(entry.getValue(), Collections.emptySet())));
        }
        return Collections.unmodifiableMap(unmodifiableMap);
    }
}