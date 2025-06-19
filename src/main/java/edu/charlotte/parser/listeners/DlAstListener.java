package edu.charlotte.parser.listeners;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicBaseListener;
import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.nodes.ASTNode;
import edu.charlotte.parser.utils.AstListenerUtils;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
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
        log.debug("Entering DL-Program rule: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_PROGRAM));
    }

    @Override
    public void exitDlProgram(DynamicDifferentialLogicParser.DlProgramContext ctx) {
        log.debug("Exiting DL-Program rule: {}", ctx.getText());
        List<ASTNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_PROGRAM, ctx.getText(), stack);
    }

    // Formula Handling
    @Override
    public void enterFormula(DynamicDifferentialLogicParser.FormulaContext ctx) {
        log.debug("Entering formula rule: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_FORMULA));
    }

    @Override
    public void exitFormula(DynamicDifferentialLogicParser.FormulaContext ctx) {
        log.debug("Exiting formula rule: {}", ctx.getText());
        List<ASTNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_FORMULA, ctx.getText(), stack);
    }

    // Program Handling
    @Override
    public void enterProgram(DynamicDifferentialLogicParser.ProgramContext ctx) {
        log.debug("Entering program rule: {}", ctx.getText());
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
        log.debug("Exiting program rule: {}", ctx.getText());
        List<ASTNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_PROGRAM_CONTEXT, ctx.getText(), stack);
    }

    @Override
    public void enterAssignmentIdentifier(DynamicDifferentialLogicParser.AssignmentIdentifierContext ctx) {
        log.debug("Entering Assignment Identifier rule: {}", ctx.getText());
        if(ctx.IDENTIFIER() != null) {
            String identifier = ctx.IDENTIFIER().getText();
            this.identifiersSet.add(identifier);
            log.debug("Found the identifier in the Assignment Identifier rule: {}", identifier);
        }
    }

    @Override
    public void enterBinaryExpr(DynamicDifferentialLogicParser.BinaryExprContext ctx) {
        log.debug("Entering Binary Expression rule: {}", ctx.getText());
        stack.push(new ASTNode(Constants.AST_NODE_DL_BINARY_EXPRESSION));
    }

    @Override
    public void exitBinaryExpr(DynamicDifferentialLogicParser.BinaryExprContext ctx) {
        log.debug("Exiting Binary Expression rule: {}", ctx.getText());
        List<ASTNode> childNodes = AstListenerUtils.exitGrammarRule(ctx, stack);
        AstListenerUtils.addChildrenToLastNodeInStack(childNodes, Constants.AST_NODE_DL_BINARY_EXPRESSION, ctx.getText(), stack);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        log.debug("Visiting terminal: '{}'", node.getText());
        stack.push(new ASTNode(node.getText()));
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