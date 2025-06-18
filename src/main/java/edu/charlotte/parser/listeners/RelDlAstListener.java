package edu.charlotte.parser.listeners;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.antlr4_parser.grammar.relational_dynamic_logic.RelationalDynamicLogicBaseListener;
import edu.charlotte.parser.antlr4_parser.grammar.relational_dynamic_logic.RelationalDynamicLogicParser;
import edu.charlotte.parser.nodes.ASTNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class RelDlAstListener extends RelationalDynamicLogicBaseListener {
    private final Stack<ASTNode> stack;
    private final Map<Character, Set<String>> identifiers;
    private char programConsidered;

    public RelDlAstListener() {
        this.stack = new Stack<>();
        identifiers = new HashMap<>();
        identifiers.put('L', new HashSet<>());
        identifiers.put('R', new HashSet<>());
        identifiers.put('G', new HashSet<>());
        this.programConsidered = 'G';
    }

    // Relation DL Program (root of the file)
    @Override
    public void enterRelDLProgram(RelationalDynamicLogicParser.RelDLProgramContext ctx) {
        stack.push(new ASTNode("Complete RelationalDLProgram"));
    }

    @Override
    public void exitRelDLProgram(RelationalDynamicLogicParser.RelDLProgramContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().addChildren(childNodes);
    }

    @Override
    public void enterRelProgram(RelationalDynamicLogicParser.RelProgramContext ctx) {
        stack.push(new ASTNode("Relational Program"));
    }

    @Override
    public void exitRelProgram(RelationalDynamicLogicParser.RelProgramContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        if(ctx.REL_TERNARY_OPERATOR() != null || ctx.REL_ASSIGNMENT_OPERATOR() != null)
            childNodes.add(new ASTNode(";"));
        stack.peek().addChildren(childNodes);
    }

    @Override
    public void enterRelFormula(RelationalDynamicLogicParser.RelFormulaContext ctx) {
        stack.push(new ASTNode("Relational Formula"));
    }

    @Override
    public void exitRelFormula(RelationalDynamicLogicParser.RelFormulaContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().addChildren(childNodes);
    }

    @Override
    public void enterRelTerm(RelationalDynamicLogicParser.RelTermContext ctx) {
        stack.push(new ASTNode("Relational Term"));
        if(ctx.PROGRAM_CONSIDERED().getText().equals("#L"))
            this.programConsidered = 'L';
        else if(ctx.PROGRAM_CONSIDERED().getText().equals("#R"))
            this.programConsidered = 'R';
        else
            this.programConsidered = 'G';

    }

    @Override
    public void exitRelTerm(RelationalDynamicLogicParser.RelTermContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        this.programConsidered = 'G';
        stack.peek().addChildren(childNodes);
    }

    // DL Formula Handling
    @Override
    public void enterFormula(RelationalDynamicLogicParser.FormulaContext ctx) {
        stack.push(new ASTNode("Formula"));
    }

    @Override
    public void exitFormula(RelationalDynamicLogicParser.FormulaContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().addChildren(childNodes);
    }

    // DL Program Handling
    @Override
    public void enterProgram(RelationalDynamicLogicParser.ProgramContext ctx) {
        stack.push(new ASTNode("Program"));
        if(ctx.IDENTIFIER() != null)
            this.addIdentifierToSet(ctx.IDENTIFIER().getText());
        if(ctx.IDENTIFIER_PRIME() != null) {
            String identifier = ctx.IDENTIFIER_PRIME().getText();
            this.addIdentifierToSet(identifier.substring(0, identifier.indexOf('\'')));
        }
    }

    @Override
    public void exitProgram(RelationalDynamicLogicParser.ProgramContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().addChildren(childNodes);
    }

    @Override
    public void enterAssignmentIdentifier(RelationalDynamicLogicParser.AssignmentIdentifierContext ctx) {
        if(ctx.IDENTIFIER() != null)
            this.addIdentifierToSet(ctx.IDENTIFIER().getText());
    }

    @Override
    public void enterBinaryExpr(RelationalDynamicLogicParser.BinaryExprContext ctx) {
        stack.push(new ASTNode("BinaryExpression"));
    }

    @Override
    public void exitBinaryExpr(RelationalDynamicLogicParser.BinaryExprContext ctx) {
        List<ASTNode> childNodes = exitGrammarRule(ctx);
        stack.peek().addChildren(childNodes);
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

    private void addIdentifierToSet(String identifier) {
        if (identifier != null && !identifier.isEmpty())
            identifiers.get(this.programConsidered).add(identifier);
//            switch (this.programConsidered) {
//                case 'L' -> this.leftProgramIdentifiersSet.add(identifier);
//                case 'R' -> this.rightProgramIdentifiersSet.add(identifier);
//                case 'G' -> this.remainingProgramIdentifiersSet.add(identifier);
//            }
    }

    // Return the final AST root node
    public ASTNode getAST() {
        return stack.isEmpty() ? null : stack.pop();
    }

    // Return the final Set of Identifiers
    public Map<Character, Set<String>> getIdentifiers() {
//        log.info("Left Program Identifiers are: {}", this.leftProgramIdentifiersSet);
//        log.info("Right Program Identifiers are: {}", this.rightProgramIdentifiersSet);
//        log.info("Remaining Program Identifiers are: {}", this.remainingProgramIdentifiersSet);
//        if(this.leftProgramIdentifiersSet.isEmpty() &&
//                this.rightProgramIdentifiersSet.isEmpty() &&
//                this.remainingProgramIdentifiersSet.isEmpty())
//            return null;
//        else {
//            Set<String> identifiersSet = new HashSet<>();
//            Stream.of(this.leftProgramIdentifiersSet, this.rightProgramIdentifiersSet, this.remainingProgramIdentifiersSet)
//                    .forEach(identifiersSet::addAll);
//            return identifiersSet;
//        }
        return identifiers.isEmpty()? null : this.identifiers;
    }
}
