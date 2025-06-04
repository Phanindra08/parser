package edu.charlotte.parser.driver_classes;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicLexer;
import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.listeners.CountingErrorsListener;
import edu.charlotte.parser.listeners.DlAstListener;
import edu.charlotte.parser.nodes.ASTNode;
import edu.charlotte.parser.parser_conversion.DLToKeYMaeraXConverter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class DlDriverClass {
    public static void main(String[] args) throws Exception {
        // Specify the path to the input file in resources
        String resourcePath = "dl_parser_examples/DlExample4";

        // Read the content of the file from resources
        String input;
        try (InputStream inputStream = DlDriverClass.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            input = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Failed to read resource file: " + e.getMessage());
            return;
        }

        // Creating lexer
        CountingErrorsListener lexerErrorListener = new CountingErrorsListener();
        DynamicDifferentialLogicLexer lexer;
        try {
            lexer = new DynamicDifferentialLogicLexer(CharStreams.fromString(input));
            // Remove default console error listener and add a custom one
            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrorListener);
        } catch (Exception e) {
            System.err.println("Lexer initialization failed: " + e.getMessage());
            return;
        }

        // Creating token stream
        CommonTokenStream tokens;
        try {
            tokens = new CommonTokenStream(lexer);
        } catch (Exception e) {
            System.err.println("Token stream creation failed: " + e.getMessage());
            return;
        }

        // Creating Parser
        CountingErrorsListener parserErrorListener = new CountingErrorsListener();
        DynamicDifferentialLogicParser parser;
        try {
            parser = new DynamicDifferentialLogicParser(tokens);
            // Remove default console error listener and add a custom one
            parser.removeErrorListeners();
            parser.addErrorListener(parserErrorListener);
        } catch (Exception e) {
            System.err.println("Parser initialization failed: " + e.getMessage());
            return;
        }

        // ParseTree reference to parse the input
        ParseTree tree;
        try {
            tree = parser.dlProgram();
            System.out.println("Lexing completed with " + lexerErrorListener.getErrorCount() + " lexer errors:");
            System.out.println("Parsing completed with " + parserErrorListener.getErrorCount() + " parser errors.");
        } catch (Exception e) {
            System.err.println("Parsing failed: " + e.getMessage());
            return;
        }

        // Only generate AST if there are no syntax errors
        ASTNode ast = null;
        Set<String> set = null;
        boolean hasLexerErrors = false;
        if (lexerErrorListener.getErrorCount() == 0 && parserErrorListener.getErrorCount() == 0) {
            // Walk the parse tree with the help of listener
            ParseTreeWalker walker = new ParseTreeWalker();
            DlAstListener listener = new DlAstListener();
            try {
                walker.walk(listener, tree);
                ast = listener.getAST(); // Getting the AST
                set = listener.getIdentifiers();
            } catch (Exception e) {
                System.err.println("Error during AST Generation: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            StringBuilder message = new StringBuilder("No AST generated due to ");
            if(lexerErrorListener.getErrorCount() > 0) {
                hasLexerErrors = true;
                message.append("lexer errors");
            }
            if(parserErrorListener.getErrorCount() > 0) {
                if(hasLexerErrors)
                    message.append(" and ");
                message.append("parser errors");
            }
                message.append(".");
            System.out.println(message);
            return;
        }
        // Print the AST if generated
        System.out.println("Generated AST is:");
        ast.printAST("", true);

        try {
            DLToKeYMaeraXConverter converter = new DLToKeYMaeraXConverter(ast);
            System.out.println("KeYmaera X Output is: " + converter.getKeYMaeraXOutput());
        } catch (Exception e) {
            System.err.println("Error during KeYmaera X conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}