package edu.charlotte.parser.antlr4_parser.basic_arithmetic;

import edu.charlotte.parser.antlr4_parser.ASTNode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class BasicArithmeticExample {
    public static void main(String[] args) throws Exception {
        // Specify the path to the input file (adjust as needed)
        String filePath = "src/main/java/edu/charlotte/parser/antlr4_parser/basic_arithmetic/BasicArithmetic2.java";

        // Read the content of the file
        String input = new String(Files.readAllBytes(Paths.get(filePath)));

        // Create lexer and parser
        BasicArithmeticLexer lexer = new BasicArithmeticLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BasicArithmeticParser parser = new BasicArithmeticParser(tokens);

        // Remove default console error listener and add a custom one
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                System.err.println("Syntax error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        // Parse the input
        ParseTree tree;
        try {
            tree = parser.compilationUnit();
            System.out.println("Parsing completed with " + parser.getNumberOfSyntaxErrors() + " syntax errors.");
        } catch (Exception e) {
            System.err.println("Parsing failed: " + e.getMessage());
            return;
        }

        // Only generate AST if there are no syntax errors
        ASTNode ast = null;
        if (parser.getNumberOfSyntaxErrors() == 0) {
            // Walk the parse tree with the listener
            ParseTreeWalker walker = new ParseTreeWalker();
            ASTListener listener = new ASTListener();
            try {
                walker.walk(listener, tree);
                ast = listener.getAST(); // Getting the AST
            } catch (Exception e) {
                System.err.println("Error during tree walking: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Print the AST if generated
        if (ast != null) {
            System.out.println("Generated AST is:");
            ast.printAST("", true);
        } else if (parser.getNumberOfSyntaxErrors() > 0)
            System.out.println("No AST is generated due to syntax errors.");
        else
            System.out.println("No AST is generated.");
    }
}

