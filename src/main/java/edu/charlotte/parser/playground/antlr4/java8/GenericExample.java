package edu.charlotte.parser.playground.antlr4.java8;

import edu.charlotte.parser.playground.Java8Lexer;
import edu.charlotte.parser.playground.Java8Parser;
import edu.charlotte.parser.playground.antlr4.ASTNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GenericExample {
    // Driver method
    public static void main(String[] args) throws Exception {
//      Read the Java file
//        String filePath = "src/main/java/edu/charlotte/parser/antlr4_parser/java8/BasicArithmetic.java";
        String filePath = "src/main/java/edu/charlotte/parser/antlr4_parser/basic_arithmetic/BasicArithmetic3.java";
//        String filePath = "src/main/java/edu/charlotte/parser/antlr4_parser/basic_arithmetic/BasicArithmeticWithoutExtension";
        String inputText = readFile(filePath);

        // Create input stream from file content
        CharStream input = CharStreams.fromString(inputText);

        // Create Lexer
        Java8Lexer lexer = new Java8Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Create Parser
        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit(); // Parse the input as a compilation unit

        // Create and attach listener
        ParseTreeWalker walker = new ParseTreeWalker();
        ASTListener listener = new ASTListener();
        walker.walk(listener, tree); // Walk the parse tree

        // Get AST and print it
        ASTNode ast = listener.getAST();
        if (ast != null) {
            System.out.println("Generated AST:");
            ast.printAST("", true); // Print AST starting from the root node
        }
    }

    // Method to read file content
    private static String readFile(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }
}
