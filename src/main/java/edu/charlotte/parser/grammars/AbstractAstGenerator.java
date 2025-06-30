package edu.charlotte.parser.grammars;

import edu.charlotte.parser.listeners.common.CountingErrorsListener;
import edu.charlotte.parser.utils.ParserUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.batch.core.configuration.annotation.StepScope;

@Getter
@Slf4j
@StepScope
public abstract class AbstractAstGenerator<L extends Lexer, P extends Parser,
        TListener extends ParseTreeListener> {

    private int lexerErrorCount;
    private int parserErrorCount;
    private TListener listener;

    public AbstractAstGenerator() {
        this.lexerErrorCount = 0;
        this.parserErrorCount = 0;
        log.info("Initialized the Ast Generator instance for '{}'.", getTypeName());
    }

    // Abstract methods to be implemented by subclasses
    protected abstract L createLexerInstance(CharStream input);
    protected abstract P createParserInstance(CommonTokenStream tokens);
    protected abstract ParseTree invokeTopLevelParseRule(P parser);
    protected abstract TListener createAstListenerInstance();
    public abstract String getTypeName();

    // Common ANTLR components methods
    protected L initializingLexer(String input) {
        CountingErrorsListener lexerErrorListener = new CountingErrorsListener();
        try {
            L lexer = createLexerInstance(CharStreams.fromString(input));
            // Removing the default console error listener and adding a custom one
            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrorListener);
            log.debug("Lexer initialized successfully for the input: {}", ParserUtils.formatInputForLogging(input));
            return lexer;
        } catch (Exception e) {
            log.error("Lexer initialization failed for the input: {}", ParserUtils.formatInputForLogging(input), e);
            throw new RuntimeException("Lexer initialization failed.", e);
        }
    }

    protected CommonTokenStream createTokenStream(L lexer) {
        try {
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            log.debug("Token stream created successfully.");
            return tokens;
        } catch (Exception e) {
            log.error("Token stream creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Token stream creation failed.", e);
        }
    }

    protected P initializingParser(CommonTokenStream tokens) {
        CountingErrorsListener parserErrorListener = new CountingErrorsListener();
        try {
            P parser = createParserInstance(tokens);
            // Remove default console error listener and adding a custom one
            parser.removeErrorListeners();
            parser.addErrorListener(parserErrorListener);
            log.debug("Parser initialized successfully.");
            return parser;
        } catch (Exception e) {
            log.error("Parser initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Parser initialization failed.", e);
        }
    }

    protected ParseTree creatingParseTree(String input) {
        L lexer = initializingLexer(input);
        CommonTokenStream tokens = createTokenStream(lexer);
        P parser = initializingParser(tokens);

        // ParseTree reference to parse the input
        ParseTree tree;
        try {
            tree = invokeTopLevelParseRule(parser);
            this.lexerErrorCount = ((CountingErrorsListener) lexer.getErrorListeners().getFirst()).getErrorCount();
            this.parserErrorCount = ((CountingErrorsListener) parser.getErrorListeners().getFirst()).getErrorCount();

            log.info("Lexing completed with {} lexer error(s).", this.lexerErrorCount);
            log.info("Parsing completed with {} parser error(s).", this.parserErrorCount);
        } catch (RecognitionException e) {
            log.error("Parsing failed due to grammar recognition error for the input: {}",
                    ParserUtils.formatInputForLogging(input), e);
            throw new RuntimeException("Parsing failed due to grammar recognition error.", e);
        } catch (Exception e) {
            log.error("Unexpected error during the parse tree creation for the input: {}",
                    ParserUtils.formatInputForLogging(input), e);
            throw new RuntimeException("Unexpected error during the parse tree creation.", e);
        }
        return tree;
    }

    public String generateAstFromInput(String input) {
        // Reset counts and listener for each input item being processed.
        this.lexerErrorCount = 0;
        this.parserErrorCount = 0;
        this.listener = createAstListenerInstance();

        ParseTree tree;
        try {
            tree = creatingParseTree(input);
            log.info("Parse tree created successfully for the input: {}.", ParserUtils.formatInputForLogging(input));
        } catch (RuntimeException e) {
            log.error("Failed to create parse tree for the input: {}", ParserUtils.formatInputForLogging(input), e);
            return "Parsing infrastructure failed: " + e.getMessage();
        }

        // Only generate AST if there are no syntax errors
        if (this.lexerErrorCount == 0 && this.parserErrorCount == 0) {
            log.info("No syntax errors. Proceeding with AST generation for {}.", getTypeName());
            ParseTreeWalker walker = new ParseTreeWalker();
            try {
                walker.walk(listener, tree);
                log.info("AST generated successfully for the {}.", getTypeName());
            } catch (Exception e) {
                log.error("Error during AST Generation for the {}: {}", getTypeName(), e.getMessage(), e);
                throw new RuntimeException("Error during AST generation for the " + getTypeName() + ".", e);
            }
            return null;
        } else {
            StringBuilder message = new StringBuilder("No AST generated for the ");
            message.append(getTypeName()).append(" due to ");
            boolean hasLexerErrors = false;
            if (this.lexerErrorCount > 0) {
                hasLexerErrors = true;
                message.append(this.lexerErrorCount).append(" lexer error(s)");
            }
            if (this.parserErrorCount > 0) {
                if (hasLexerErrors) {
                    message.append(" and ");
                }
                message.append(this.parserErrorCount).append(" parser error(s)");
            }
            message.append(".");
            String finalMessage = message.toString();
            log.warn("{}", finalMessage);
            return finalMessage;
        }
    }
}