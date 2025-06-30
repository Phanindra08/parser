package edu.charlotte.parser.utils;

import edu.charlotte.parser.ast.nodes.AstNode;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

@Slf4j
public class AstListenerUtils {
    private AstListenerUtils() {}
    public static List<AstNode> exitGrammarRule(ParserRuleContext ctx, Stack<AstNode> stack) {
        int expectedChildrenCount = ctx.getChildCount();
        if (stack.size() < expectedChildrenCount) {
            log.error("Stack underflow: Expected {} children for the rule '{}', but the stack has only {} elements.",
                    expectedChildrenCount, ctx.getText(), stack.size());
            throw new IllegalStateException("Critical AST construction error for the rule: " + ctx.getText());
        }

        List<AstNode> grammarRuleChildNodes = new ArrayList<>(expectedChildrenCount);
        for (int index = 0; index < expectedChildrenCount; index++)
            grammarRuleChildNodes.add(stack.pop());
        log.debug("Popped all the {} children from stack for rule '{}'.", expectedChildrenCount, ctx.getText());
        Collections.reverse(grammarRuleChildNodes);
        return grammarRuleChildNodes;
    }

    public static void addChildrenToLastNodeInStack(List<AstNode> childNodes, String grammarNodeName, String contextText, Stack<AstNode> stack) {
        if (childNodes == null) {
            log.warn("Attempted to add a null list as children to the node on top of the stack for the rule '{}' (context: '{}')",
                    grammarNodeName, contextText);
            return;
        }

        if (!stack.isEmpty()) {
            stack.peek().addChildren(childNodes);
            log.debug("Added {} children to the node on top of the stack for the rule '{}' (context: '{}').",
                    childNodes.size(), grammarNodeName, contextText);
        } else {
            log.error("Stack is unexpectedly empty when exiting the rule {} for the context: {}, indicating a critical logic error.", grammarNodeName, contextText);
            throw new IllegalStateException("Cannot add children as the Stack is empty for the rule: " + grammarNodeName);
        }
    }
}