package edu.charlotte.parser.basic_arithmetic;

import edu.charlotte.parser.antlr4_parser.ASTNode;
import edu.charlotte.parser.antlr4_parser.basic_arithmetic.ASTListener;
import edu.charlotte.parser.antlr4_parser.basic_arithmetic.BasicArithmeticLexer;
import edu.charlotte.parser.antlr4_parser.basic_arithmetic.BasicArithmeticParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class BasicArithmeticParserTest {
    private ByteArrayOutputStream errContent;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        // Capture System.err output for testing error messages
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        // Restore System.err
        System.setErr(originalErr);
    }

    // Helper method to parse input and return results
    private ParserTestResult parseInput(String input) {
        // Create lexer and parser
        BasicArithmeticLexer lexer = new BasicArithmeticLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BasicArithmeticParser parser = new BasicArithmeticParser(tokens);

        // Custom error listener to capture errors
        StringBuilder errorMessages = new StringBuilder();
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                errorMessages.append("Syntax error at line ").append(line).append(":")
                        .append(charPositionInLine).append(" - ").append(msg).append("\n");
            }
        });

        // Parse and walk the tree
        ASTNode ast = null;
        int syntaxErrors = 0;
        try {
            ParseTree tree = parser.compilationUnit();
            syntaxErrors = parser.getNumberOfSyntaxErrors();
            if (syntaxErrors == 0) {
                ParseTreeWalker walker = new ParseTreeWalker();
                ASTListener listener = new ASTListener();
                walker.walk(listener, tree);
                ast = listener.getAST();
            }
        } catch (Exception e) {
            errorMessages.append("Parsing failed: ").append(e.getMessage()).append("\n");
        }

        return new ParserTestResult(ast, syntaxErrors, errorMessages.toString());
    }

    // Test valid BasicArithmetic1.java
    @Test
    public void testValidBasicArithmetic1() throws Exception {
        // Read BasicArithmetic1.java content
        String filePath = "src/main/java/edu/charlotte/parser/antlr4_parser/basic_arithmetic/BasicArithmeticWithoutExtension";
        String input = new String(Files.readAllBytes(Paths.get(filePath)));

        ParserTestResult result = parseInput(input);

        // Verify no syntax errors
        assertEquals(0, result.syntaxErrors, "Expected no syntax errors for valid code");
        assertTrue(result.errorMessages.isEmpty(), "Expected no error messages for valid code");

        // Verify AST is generated
//        assertNotNull(result.ast, "Expected a non-null AST for valid code");
//        assertEquals("CompilationUnit", result.ast.getType(), "Root node should be CompilationUnit");
//        assertEquals("CompilationUnit", result.ast.getValue(), "Root node value should be CompilationUnit");
//
//        // Verify key AST nodes
//        ASTNode packageNode = result.ast.getChildren().get(0);
//        assertEquals("Package", packageNode.getType(), "Expected Package node");
//        assertEquals("edu.charlotte.parser.antlr4_parser.basic_arithmetic", packageNode.getValue(),
//                "Expected correct package name");
//
//        ASTNode classNode = result.ast.getChildren().get(1);
//        assertEquals("Class", classNode.getType(), "Expected Class node");
//        assertEquals("BasicArithmetic1", classNode.getValue(), "Expected class name BasicArithmetic1");
//
//        ASTNode methodNode = classNode.getChildren().get(0);
//        assertEquals("Method", methodNode.getType(), "Expected Method node");
//        assertEquals("main", methodNode.getValue(), "Expected method name main");
//
//        // Verify variable declarations (e.g., double num1 = 10.8)
//        ASTNode num1Decl = methodNode.getChildren().get(0);
//        assertEquals("Type", num1Decl.getType(), "Expected Type node for num1");
//        assertEquals("double", num1Decl.getValue(), "Expected type double");
//        ASTNode varDeclNum1 = num1Decl.getChildren().get(0);
//        assertEquals("VarDecl", varDeclNum1.getType(), "Expected VarDecl for num1");
//        assertEquals("num1", varDeclNum1.getValue(), "Expected variable name num1");
//        ASTNode num1Init = varDeclNum1.getChildren().get(0);
//        assertEquals("Literal", num1Init.getType(), "Expected Literal for num1 initializer");
//        assertEquals("10.8", num1Init.getValue(), "Expected initializer value 10.8");
//
//        // Verify sum = num1 + num2
//        ASTNode sumDecl = methodNode.getChildren().get(2);
//        assertEquals("Type", sumDecl.getType(), "Expected Type node for sum");
//        ASTNode varDeclSum = sumDecl.getChildren().get(0);
//        ASTNode sumInit = varDeclSum.getChildren().get(0);
//        assertEquals("Op", sumInit.getType(), "Expected Op node for sum initializer");
//        assertEquals("+", sumInit.getValue(), "Expected + operator");
//        assertEquals("Var", sumInit.getChildren().get(0).getType(), "Expected Var node for num1");
//        assertEquals("num1", sumInit.getChildren().get(0).getValue(), "Expected variable num1");
//        assertEquals("Var", sumInit.getChildren().get(1).getType(), "Expected Var node for num2");
//        assertEquals("num2", sumInit.getChildren().get(1).getValue(), "Expected variable num2");
    }

    // Test invalid Java code
//    @Test
//    public void testInvalidJavaCode() {
//        // Invalid code with syntax errors
//        String invalidInput = """
//                package edu.charlotte.parser;
//                public class Invalid {
//                    public static void main(String[] args) {
//                        double num1 = 10.8
//                        double num2 = 12; // Missing semicolon
//                    }
//                }
//                """;
//
//        ParserTestResult result = parseInput(invalidInput);
//
//        // Verify syntax errors are detected
//        assertTrue(result.syntaxErrors > 0, "Expected syntax errors for invalid code");
//        assertFalse(result.errorMessages.isEmpty(), "Expected error messages for invalid code");
//        assertTrue(result.errorMessages.contains("Syntax error"), "Expected 'Syntax error' in messages");
//
//        // Verify no AST is generated
//        assertNull(result.ast, "Expected null AST for invalid code");
//    }

    // Test empty input
//    @Test
//    public void testEmptyInput() {
//        String emptyInput = "";
//
//        ParserTestResult result = parseInput(emptyInput);
//
//        // Empty input may produce errors depending on grammar
//        assertTrue(result.syntaxErrors >= 0, "Expected non-negative error count for empty input");
//        assertNull(result.ast, "Expected null AST for empty input");
//        assertFalse(result.errorMessages.contains("Parsing failed"), "Expected no 'Parsing failed' for empty input");
//    }

    // Test error handling with malformed input
//    @Test
//    public void testMalformedInput() {
//        // Malformed code with unclosed brace
//        String malformedInput = """
//                package edu.charlotte.parser;
//                public class Malformed {
//                    public static void main(String[] args) {
//                        double num1 = 10.8;
//                """;
//
//        ParserTestResult result = parseInput(malformedInput);
//
//        // Verify syntax errors
//        assertTrue(result.syntaxErrors > 0, "Expected syntax errors for malformed code");
//        assertTrue(result.errorMessages.contains("Syntax error"), "Expected 'Syntax error' in messages");
//
//        // Verify no AST
//        assertNull(result.ast, "Expected null AST for malformed code");
//    }

    // Helper class to store parsing results
    private class ParserTestResult {
        ASTNode ast;
        int syntaxErrors;
        String errorMessages;

        ParserTestResult(ASTNode ast, int syntaxErrors, String errorMessages) {
            this.ast = ast;
            this.syntaxErrors = syntaxErrors;
            this.errorMessages = errorMessages;
        }
    }
}
