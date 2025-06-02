package edu.charlotte.parser.listeners;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicBaseListener;
import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.nodes.ASTNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class DlAstListener extends DynamicDifferentialLogicBaseListener {
    private final Stack<ASTNode> stack;
    private final Set<String> identifiersSet;

    public DlAstListener() {
        this.stack = new Stack<>();
        this.identifiersSet = new HashSet<>();
    }

    // DL Program (root of the file)
    @Override
    public void enterDlProgram(DynamicDifferentialLogicParser.DlProgramContext ctx) {
        stack.push(new ASTNode("DLProgram"));
    }

    @Override
    public void exitDlProgram(DynamicDifferentialLogicParser.DlProgramContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().setChildren(childNodes);
    }

    // Formula Handling
    @Override
    public void enterFormula(DynamicDifferentialLogicParser.FormulaContext ctx) {
        stack.push(new ASTNode("Formula"));
    }

    @Override
    public void exitFormula(DynamicDifferentialLogicParser.FormulaContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().setChildren(childNodes);
    }

    // Program Handling
    @Override
    public void enterProgram(DynamicDifferentialLogicParser.ProgramContext ctx) {
        stack.push(new ASTNode("Program"));
        if(ctx.IDENTIFIER() != null)
            this.identifiersSet.add(ctx.IDENTIFIER().getText());
        if(ctx.IDENTIFIER_PRIME() != null) {
            String identifier = ctx.IDENTIFIER_PRIME().getText();
            this.identifiersSet.add(identifier.substring(0, identifier.indexOf('\'')));
        }
    }

    @Override
    public void exitProgram(DynamicDifferentialLogicParser.ProgramContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().setChildren(childNodes);
    }

    @Override
    public void enterAssignmentIdentifier(DynamicDifferentialLogicParser.AssignmentIdentifierContext ctx) {
        if(ctx.IDENTIFIER() != null)
            this.identifiersSet.add(ctx.IDENTIFIER().getText());
    }

    @Override
    public void enterBinaryExpr(DynamicDifferentialLogicParser.BinaryExprContext ctx) {
        stack.push(new ASTNode("BinaryExpression"));
    }

    @Override
    public void exitBinaryExpr(DynamicDifferentialLogicParser.BinaryExprContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().setChildren(childNodes);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        stack.push(new ASTNode(node.getText()));
    }

    private List<ASTNode> exitGrammarRule(ParserRuleContext ctx) {
        List<ASTNode> grammarRuleChildNodes = new ArrayList<>();
        for(int index = ctx.getChildCount() - 1; index >= 0; index--)
            grammarRuleChildNodes.addFirst(stack.pop());
        return grammarRuleChildNodes;
    }

    // Return the final AST root node
    public ASTNode getAST() {
        return stack.isEmpty() ? null : stack.pop();
    }

    // Return the final Set of Identifiers
    public Set<String> getIdentifiers() {
        return this.identifiersSet.isEmpty() ? null : this.identifiersSet;
    }
}