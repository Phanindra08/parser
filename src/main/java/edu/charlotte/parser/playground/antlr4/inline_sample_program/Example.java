package edu.charlotte.parser.playground.antlr4.inline_sample_program;

import edu.charlotte.parser.playground.HelloLexer;
import edu.charlotte.parser.playground.HelloParser;
import edu.charlotte.parser.playground.antlr4.ASTNode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Example {
    public static void main(String[] args) throws Exception {
        String input = "Hello World";
        HelloLexer lexer = new HelloLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HelloParser parser = new HelloParser(tokens);
        ParseTree tree = parser.start();

        ParseTreeWalker walker = new ParseTreeWalker();
        ASTListener listener = new ASTListener();
        walker.walk(listener, tree); // Walk the parse tree

        // Get AST and print it
        ASTNode ast = listener.getAST();
        if (ast != null) {
            System.out.println("Generated AST is:");
            ast.printAST("", true);
        }
    }
}