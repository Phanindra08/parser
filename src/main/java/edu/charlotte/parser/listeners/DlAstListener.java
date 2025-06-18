package edu.charlotte.parser.listeners;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicBaseListener;
import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.nodes.ASTNode;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

@Slf4j
public class DlAstListener extends DynamicDifferentialLogicBaseListener {
    private final Stack<ASTNode> stack;
    private final Set<String> identifiersSet;

    public DlAstListener() {
        this.stack = new Stack<>();
        this.identifiersSet = new HashSet<>();
        log.debug("DlAstListener initialized.");
    }

    // DL Program (root of the file)
    @Override
    public void enterDlProgram(DynamicDifferentialLogicParser.DlProgramContext ctx) {
        log.debug("Entering DL-Program: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_PROGRAM));
    }

    @Override
    public void exitDlProgram(DynamicDifferentialLogicParser.DlProgramContext ctx) {
        log.debug("Exiting DL-Program: {}", ctx.getText());
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_PROGRAM, ctx.getText());
    }

    // Formula Handling
    @Override
    public void enterFormula(DynamicDifferentialLogicParser.FormulaContext ctx) {
        log.debug("Entering formula: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_FORMULA));
    }

    @Override
    public void exitFormula(DynamicDifferentialLogicParser.FormulaContext ctx) {
        log.debug("Exiting formula: {}", ctx.getText());
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_FORMULA, ctx.getText());
    }

    // Program Handling
    @Override
    public void enterProgram(DynamicDifferentialLogicParser.ProgramContext ctx) {
        log.debug("Entering program: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_PROGRAM_CONTEXT));
        if(ctx.IDENTIFIER() != null) {
            this.identifiersSet.add(ctx.IDENTIFIER().getText());
            log.debug("Found identifier '{}' in the program context.", ctx.IDENTIFIER().getText());
        }
        if(ctx.IDENTIFIER_PRIME() != null) {
            String identifierPrime = ctx.IDENTIFIER_PRIME().getText();
            if (identifierPrime.endsWith("'")) {
                String identifier = identifierPrime.substring(0, identifierPrime.length() - 1);
                this.identifiersSet.add(identifier);
                log.debug("Found primed identifier '{}' in the program context.", identifierPrime);
            } else {
                log.warn("Identifier prime '{}' does not end with a prime(') character as expected. Adding full text to the Array.", identifierPrime);
                this.identifiersSet.add(identifierPrime);
            }
        }
    }

    @Override
    public void exitProgram(DynamicDifferentialLogicParser.ProgramContext ctx) {
        log.debug("Exiting program: {}", ctx.getText());
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_PROGRAM_CONTEXT, ctx.getText());
    }

    @Override
    public void enterAssignmentIdentifier(DynamicDifferentialLogicParser.AssignmentIdentifierContext ctx) {
        log.debug("Entering Assignment Identifier: {}", ctx.getText());
        if(ctx.IDENTIFIER() != null) {
            String identifier = ctx.IDENTIFIER().getText();
            this.identifiersSet.add(identifier);
            log.debug("Found the identifier in the Assignment: {}", identifier);
        }
    }

    @Override
    public void enterBinaryExpr(DynamicDifferentialLogicParser.BinaryExprContext ctx) {
        log.debug("Entering Binary Expression: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_BINARY_EXPRESSION));
    }

    @Override
    public void exitBinaryExpr(DynamicDifferentialLogicParser.BinaryExprContext ctx) {
        log.debug("Exiting Binary Expression: {}", ctx.getText());
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_BINARY_EXPRESSION, ctx.getText());
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        log.debug("Visiting terminal: '{}'", node.getText());
        stack.push(new ASTNode(node.getText()));
    }

    private List<ASTNode> exitGrammarRule(ParserRuleContext ctx) {
        int expectedChildrenCount = ctx.getChildCount();
        if (stack.size() < expectedChildrenCount) {
            log.error("Stack underflow: Expected {} children for the rule '{}', but the stack has only {} elements.",
                    expectedChildrenCount, ctx.getText(), stack.size());
            throw new IllegalStateException("Stack underflow during the AST construction for the rule: " + ctx.getText());
        }

        List<ASTNode> grammarRuleChildNodes = new ArrayList<>(expectedChildrenCount);
        for (int index = 0; index < expectedChildrenCount; index++)
            grammarRuleChildNodes.addFirst(stack.pop());
        return grammarRuleChildNodes;
    }

    private void addChildrenToLastNodeInStack(List<ASTNode> childNodes, String grammarNode, String contextText) {
        if (!stack.isEmpty())
            stack.peek().addChildren(childNodes);
        else {
            log.error("Stack is unexpectedly empty when exiting {} for the context: {}, indicating a critical logic error.", grammarNode, contextText);
            throw new IllegalStateException("Cannot add children as the Stack is empty for the " + grammarNode + " node.");
        }
    }

    // Return the final AST root node
    public ASTNode getAST() {
        if(stack.size() > 1)
            log.warn("Stack contains more than one element after AST generation. The might be a possible issue in listener logic. " +
                    "Stack size is: {} and the contents are: {}", stack.size(), stack);
        return stack.isEmpty() ? null : stack.pop();
    }

    // Return the final Set of Identifiers
    public Set<String> getIdentifiers() {
        return Collections.unmodifiableSet(this.identifiersSet);
    }
}