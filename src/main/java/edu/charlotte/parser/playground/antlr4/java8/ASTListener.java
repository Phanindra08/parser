package edu.charlotte.parser.playground.antlr4.java8;

import edu.charlotte.parser.playground.Java8BaseListener;
import edu.charlotte.parser.playground.Java8Parser;
import edu.charlotte.parser.playground.antlr4.ASTNode;

import java.util.Stack;
import java.util.stream.Collectors;

public class ASTListener extends Java8BaseListener {
    private Stack<ASTNode> stack = new Stack<>();

    // Compilation Unit (root of the Java file)
    @Override
    public void enterCompilationUnit(Java8Parser.CompilationUnitContext ctx) {
        ASTNode node = new ASTNode("CompilationUnit", "CompilationUnit");
        stack.push(node);
    }

    @Override
    public void exitCompilationUnit(Java8Parser.CompilationUnitContext ctx) {
        // Final AST remains on the stack
    }

    // Package Declaration
    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        String packageName = ctx.Identifier().stream()
                .map(Object::toString)
                .collect(Collectors.joining("."));
        ASTNode node = new ASTNode("Package", packageName);
        stack.push(node);
    }

    @Override
    public void exitPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        ASTNode packageNode = stack.pop();
        ASTNode compilationUnitNode = stack.peek();
        compilationUnitNode.addChild(packageNode);
    }

    // Import Declaration
    @Override
    public void enterSingleTypeImportDeclaration(Java8Parser.SingleTypeImportDeclarationContext ctx) {
        String importName = ctx.typeName().getText();
        ASTNode node = new ASTNode("Import", importName);
        stack.push(node);
    }

    @Override
    public void exitSingleTypeImportDeclaration(Java8Parser.SingleTypeImportDeclarationContext ctx) {
        ASTNode importNode = stack.pop();
        ASTNode compilationUnitNode = stack.peek();
        compilationUnitNode.addChild(importNode);
    }

    // Class Declaration
    @Override
    public void enterNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
        String className = ctx.Identifier().getText();
        ASTNode node = new ASTNode("Class", className);
        stack.push(node);
    }

    @Override
    public void exitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {
        ASTNode classNode = stack.pop();
        ASTNode parentNode = stack.peek();
        parentNode.addChild(classNode);
    }

    // Method Declaration
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String methodName = ctx.methodHeader().methodDeclarator().Identifier().getText();
        ASTNode node = new ASTNode("Method", methodName);
        stack.push(node);
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        ASTNode methodNode = stack.pop();
        ASTNode parentNode = stack.peek();
        parentNode.addChild(methodNode);
    }

    public void enterLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        String varName = ctx.variableDeclaratorList().variableDeclarator(0).variableDeclaratorId().Identifier().getText();
        String typeName = ctx.unannType().getText();
        ASTNode node = new ASTNode("VarDecl", varName);
        node.addChild(new ASTNode("Type", typeName));
        stack.push(node);
    }

    @Override
    public void exitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        ASTNode varNode = stack.pop();
        if (ctx.variableDeclaratorList().variableDeclarator(0).variableInitializer() != null && !stack.isEmpty()) {
            ASTNode exprNode = stack.pop(); // Expression from variableInitializer
            varNode.addChild(exprNode);
        }
        ASTNode parentNode = stack.peek();
        parentNode.addChild(varNode);
    }

    // Assignment
    @Override
    public void enterAssignment(Java8Parser.AssignmentContext ctx) {
        String varName = ctx.leftHandSide().getText();
        ASTNode node = new ASTNode("Assign", varName);
        stack.push(node);
    }

    @Override
    public void exitAssignment(Java8Parser.AssignmentContext ctx) {
        ASTNode exprNode = stack.pop(); // Expression on the right-hand side
        ASTNode assignNode = stack.pop(); // Assignment node
        assignNode.addChild(exprNode);
        ASTNode parentNode = stack.peek();
        parentNode.addChild(assignNode);
    }

    // Method Invocation
    @Override
    public void enterMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        String methodName = ctx.methodName() != null ? ctx.methodName().getText() :
                (ctx.Identifier() != null ? ctx.getText() : ctx.getText());
        ASTNode node = new ASTNode("MethodCall", methodName);
        stack.push(node);
    }

    @Override
    public void exitMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        ASTNode methodNode = stack.pop();
        if (ctx.argumentList() != null && !stack.isEmpty()) {
            ASTNode argNode = stack.pop(); // Argument expression
            methodNode.addChild(argNode);
        }
        ASTNode parentNode = stack.peek();
        parentNode.addChild(methodNode);
    }

    // Class Instance Creation (e.g., new Scanner(...))
    @Override
    public void enterClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx) {
        String typeName = ctx.Identifier(ctx.Identifier().size() - 1).getText();
        ASTNode node = new ASTNode("New", typeName);
        stack.push(node);
    }

    @Override
    public void exitClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx) {
        ASTNode newNode = stack.pop();
        if (ctx.argumentList() != null && !stack.isEmpty()) {
            ASTNode argNode = stack.pop(); // Argument expression
            newNode.addChild(argNode);
        }
        ASTNode parentNode = stack.peek();
        parentNode.addChild(newNode);
    }

    // Additive Expression (e.g., num1 + num2)
    @Override
    public void enterAdditiveExpression(Java8Parser.AdditiveExpressionContext ctx) {
        if (ctx.getChildCount() > 1) { // Check if it's a binary operation
            String op = ctx.getChild(1).getText(); // Operator (+ or -)
            ASTNode node = new ASTNode("Op", op);
            stack.push(node);
        }
    }

    @Override
    public void exitAdditiveExpression(Java8Parser.AdditiveExpressionContext ctx) {
        if (ctx.getChildCount() > 1 && stack.size() >= 3) { // Ensure binary operation with enough nodes
            ASTNode right = stack.pop();
            ASTNode opNode = stack.pop();
            ASTNode left = stack.pop();
            opNode.addChild(left);
            opNode.addChild(right);
            stack.push(opNode);
        }
    }

    // Multiplicative Expression (e.g., num1 * num2)
    @Override
    public void enterMultiplicativeExpression(Java8Parser.MultiplicativeExpressionContext ctx) {
        if (ctx.getChildCount() > 1) { // Check if it's a binary operation
            String op = ctx.getChild(1).getText(); // Operator (*, /, or %)
            ASTNode node = new ASTNode("Op", op);
            stack.push(node);
        }
    }

    @Override
    public void exitMultiplicativeExpression(Java8Parser.MultiplicativeExpressionContext ctx) {
        if (ctx.getChildCount() > 1 && stack.size() >= 3) { // Ensure binary operation with enough nodes
            ASTNode right = stack.pop();
            ASTNode opNode = stack.pop();
            ASTNode left = stack.pop();
            opNode.addChild(left);
            opNode.addChild(right);
            stack.push(opNode);
        }
    }

    // Literal (e.g., strings)
    @Override
    public void enterLiteral(Java8Parser.LiteralContext ctx) {
        ASTNode node = new ASTNode("Literal", ctx.getText());
        stack.push(node);
    }

    // Variable Reference (e.g., num1, num2)
    @Override
    public void enterExpressionName(Java8Parser.ExpressionNameContext ctx) {
        ASTNode node = new ASTNode("Var", ctx.getText());
        stack.push(node);
    }

    // Return the final AST
    public ASTNode getAST() {
        return stack.isEmpty() ? null : stack.pop();
    }
}