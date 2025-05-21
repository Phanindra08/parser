package edu.charlotte.parser.antlr4_parser.basic_arithmetic;

import edu.charlotte.parser.antlr4_parser.ASTNode;

import java.util.Stack;

public class ASTListener extends BasicArithmeticBaseListener {
    private Stack<ASTNode> stack = new Stack<>();

    // Compilation Unit (root of the file)
    @Override
    public void enterCompilationUnit(BasicArithmeticParser.CompilationUnitContext ctx) {
        ASTNode node = new ASTNode("CompilationUnit", "CompilationUnit");
        stack.push(node);
    }

    // Package Declaration
    @Override
    public void enterPackageDeclaration(BasicArithmeticParser.PackageDeclarationContext ctx) {
        String packageName = ctx.qualifiedName().getText();
        ASTNode node = new ASTNode("Package", packageName);
        stack.push(node);
    }

    @Override
    public void exitPackageDeclaration(BasicArithmeticParser.PackageDeclarationContext ctx) {
        ASTNode packageNode = stack.pop();
        ASTNode compilationUnitNode = stack.peek();
        compilationUnitNode.addChild(packageNode);
    }

    // Import Declaration
    @Override
    public void enterImportDeclaration(BasicArithmeticParser.ImportDeclarationContext ctx) {
        String importName = ctx.qualifiedName().getText();
        ASTNode node = new ASTNode("Import", importName);
        stack.push(node);
    }

    @Override
    public void exitImportDeclaration(BasicArithmeticParser.ImportDeclarationContext ctx) {
        ASTNode importNode = stack.pop();
        ASTNode compilationUnitNode = stack.peek();
        compilationUnitNode.addChild(importNode);
    }

    // Class Declaration
    @Override
    public void enterClassDeclaration(BasicArithmeticParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        ASTNode node = new ASTNode("Class", className);
        stack.push(node);
    }

    @Override
    public void exitClassDeclaration(BasicArithmeticParser.ClassDeclarationContext ctx) {
        ASTNode classNode = stack.pop();
        ASTNode parentNode = stack.peek();
        parentNode.addChild(classNode);
    }

    // Method Declaration
    @Override
    public void enterMethodDeclaration(BasicArithmeticParser.MethodDeclarationContext ctx) {
        String methodName = ctx.IDENTIFIER().getText();
        ASTNode node = new ASTNode("Method", methodName);
        stack.push(node);
    }

    @Override
    public void exitMethodDeclaration(BasicArithmeticParser.MethodDeclarationContext ctx) {
        ASTNode methodNode = stack.pop();
        ASTNode parentNode = stack.peek();
        parentNode.addChild(methodNode);
    }

    // Field Declaration (Variable Declaration)
    @Override
    public void enterFieldDeclaration(BasicArithmeticParser.FieldDeclarationContext ctx) {
        String type = ctx.primitiveType().getText();
        ASTNode typeNode = new ASTNode("Type", type);
        stack.push(typeNode);
    }

    @Override
    public void exitFieldDeclaration(BasicArithmeticParser.FieldDeclarationContext ctx) {
        ASTNode literalNode = stack.pop(); // Pop the literal node
        // Handle all variable declarators under this type
        int identifierCount = ctx.variableDeclarators().IDENTIFIER().size();
        for (int i = 0; i < identifierCount; i++) {
            String varName = ctx.variableDeclarators().IDENTIFIER(i).getText();
            ASTNode varDeclNode = new ASTNode("VarDecl", varName);
            // Check if there's an initializer for this variable
            if (ctx.variableDeclarators().expression() != null && !stack.isEmpty()) {
                ASTNode typeNode = stack.pop(); // Pop the initializer expression
                varDeclNode.addChild(typeNode); // Attach type to VarDecl Node
            }
            literalNode.addChild(varDeclNode); // Add VarDecl Node to Literal Node
        }
        // Add the literal node to the parent (e.g., Method or Class)
        ASTNode parentNode = stack.peek();
        parentNode.addChild(literalNode);
    }

    // Assignment Expression
    @Override
    public void enterAssignmentExpression(BasicArithmeticParser.AssignmentExpressionContext ctx) {
        ASTNode assignNode = new ASTNode("Assign", ctx.IDENTIFIER().getText());
        stack.push(assignNode);
    }

    @Override
    public void exitAssignmentExpression(BasicArithmeticParser.AssignmentExpressionContext ctx) {
        if (stack.size() >= 2) {
            ASTNode exprNode = stack.pop(); // Pop the right-hand side expression
            ASTNode assignNode = stack.pop(); // Pop the Assign node
            assignNode.addChild(exprNode); // Expression is child of Assign
            stack.push(assignNode);
        }
    }

    // Method Call Expression
    @Override
    public void enterMethodCallExpression(BasicArithmeticParser.MethodCallExpressionContext ctx) {
        String methodName = ctx.methodCall().qualifiedName().getText() + "(" +
                (ctx.methodCall().argumentList() != null ? ctx.methodCall().argumentList().getText() : "") + ")";
        ASTNode node = new ASTNode("MethodCallExpression", methodName);
        stack.push(node);
    }

    @Override
    public void exitMethodCallExpression(BasicArithmeticParser.MethodCallExpressionContext ctx) {
        if (ctx.methodCall().argumentList() != null && stack.size() > 1) {
            ASTNode argNode = stack.pop();     // Pop the Argument expression
            ASTNode methodNode = stack.pop();  // Pop the MethodCall node
            methodNode.addChild(argNode);      // Argument is child of MethodCall Node
            stack.push(methodNode);            // Push back the combined node
        }
    }

    // Method Call
    @Override
    public void enterMethodCall(BasicArithmeticParser.MethodCallContext ctx) {
        // Skip if this methodCall is part of a MethodCallExpression
        if (!(ctx.getParent() instanceof BasicArithmeticParser.MethodCallExpressionContext)) {
            String methodName = ctx.qualifiedName().getText() + "(" + (ctx.argumentList() != null ? ctx.argumentList().getText() : "") + ")";
            ASTNode node = new ASTNode("MethodCall", methodName);
            stack.push(node);
        }
    }

    @Override
    public void exitMethodCall(BasicArithmeticParser.MethodCallContext ctx) {
        if (!(ctx.getParent() instanceof BasicArithmeticParser.MethodCallExpressionContext)) {
            if (ctx.argumentList() != null && stack.size() > 1) {
                ASTNode argNode = stack.pop(); // Pop the Argument expression
                ASTNode methodNode = stack.pop(); // Pop the MethodCall node
                methodNode.addChild(argNode); // Argument is child of MethodCall Node
                stack.push(methodNode); // Push back the combined node
            } else if (stack.size() > 1) {
                ASTNode methodNode = stack.pop();
                stack.peek().addChild(methodNode);
            }
        }
    }

    // Expression Handling
    @Override
    public void enterIntegerLiteral(BasicArithmeticParser.IntegerLiteralContext ctx) {
        ASTNode node = new ASTNode("Literal", ctx.NUMBERS().getText());
        stack.push(node);
    }

    @Override
    public void enterStringLiteral(BasicArithmeticParser.StringLiteralContext ctx) {
        ASTNode node = new ASTNode("Literal", ctx.STRING().getText());
        stack.push(node);
    }

    @Override
    public void enterVariable(BasicArithmeticParser.VariableContext ctx) {
        ASTNode node = new ASTNode("Var", ctx.IDENTIFIER().getText());
        stack.push(node);
    }

    @Override
    public void enterBinaryExpr(BasicArithmeticParser.BinaryExprContext ctx) {
        String op = ctx.getChild(1).getText(); // Operator (+, -, *, /)
        ASTNode opNode = new ASTNode("Op", op);
        stack.push(opNode);
    }

    @Override
    public void exitBinaryExpr(BasicArithmeticParser.BinaryExprContext ctx) {
        if (stack.size() >= 3) {
            ASTNode right = stack.pop();
            ASTNode left = stack.pop();
            ASTNode opNode = stack.pop();
            left.addChild(opNode);
            left.addChild(right);
            stack.push(left);
        }
    }

    @Override
    public void exitExpressionStatement(BasicArithmeticParser.ExpressionStatementContext ctx) {
        if (stack.size() > 1) {
            ASTNode node = stack.pop();
            stack.peek().addChild(node);
        }
    }

    // Parenthesized Expression (e.g., (num1 + num2))
    @Override
    public void enterParenthesizedExpression(BasicArithmeticParser.ParenthesizedExpressionContext ctx) {
        ASTNode node = new ASTNode("Parentheses", "");
        stack.push(node);
    }

    @Override
    public void exitParenthesizedExpression(BasicArithmeticParser.ParenthesizedExpressionContext ctx) {
        ASTNode exprNode = stack.pop();
        ASTNode parentNode = stack.peek();
        parentNode.addChild(exprNode);
    }

    // Return the final AST
    public ASTNode getAST() {
        return stack.isEmpty() ? null : stack.pop();
    }
}