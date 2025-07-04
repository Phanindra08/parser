package edu.charlotte.parser.playground.antlr4.inline_sample_program;

import edu.charlotte.parser.playground.HelloBaseListener;
import edu.charlotte.parser.playground.HelloParser;
import edu.charlotte.parser.playground.antlr4.ASTNode;

import java.util.Stack;

public class ASTListener extends HelloBaseListener {
    private Stack<ASTNode> stack = new Stack<>();

    @Override
    public void enterGreeting(HelloParser.GreetingContext ctx) {
        // Create a Greeting node
        ASTNode node = new ASTNode("Greeting", "Hello");
        stack.push(node);
    }

    @Override
    public void exitGreeting(HelloParser.GreetingContext ctx) {
        ASTNode greetingNode = stack.pop(); // Get the "Hello" node
        ASTNode idNode = new ASTNode("ID", ctx.ID().getText()); // Create ID node
        greetingNode.addChild(idNode);
        stack.push(greetingNode); // Push back the complete greeting node
    }

    @Override
    public void exitStart(HelloParser.StartContext ctx) {
        // The final AST node (greeting) will remain at the top of the stack
    }

    public ASTNode getAST() {
        return stack.isEmpty() ? null : stack.pop();
    }
}