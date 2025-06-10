package edu.charlotte.parser.parser_for_grammars;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicLexer;
import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.listeners.CountingErrorsListener;
import edu.charlotte.parser.listeners.DlAstListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@Component
public class GenerateAST {
    private int lexerErrorCount;
    private int parserErrorCount;
    private DlAstListener listener;

    private DynamicDifferentialLogicLexer creatingLexer(String input) {
        DynamicDifferentialLogicLexer lexer;
        CountingErrorsListener lexerErrorListener = new CountingErrorsListener();
        try {
            lexer = new DynamicDifferentialLogicLexer(CharStreams.fromString(input));
            // Remove default console error listener and add a custom one
            lexer.removeErrorListeners();
            lexer.addErrorListener(lexerErrorListener);
        } catch (Exception e) {
            log.error("Lexer initialization failed: {}", e.getMessage());
            throw e;
        }
        return lexer;
    }

    private CommonTokenStream creatingToken(DynamicDifferentialLogicLexer lexer) {
        CommonTokenStream tokens;
        try {
            tokens = new CommonTokenStream(lexer);
        } catch (Exception e) {
            log.error("Token stream creation failed: {}", e.getMessage());
            throw e;
        }
        return tokens;
    }

    private DynamicDifferentialLogicParser creatingParser(CommonTokenStream tokens) {
        CountingErrorsListener parserErrorListener = new CountingErrorsListener();
        DynamicDifferentialLogicParser parser;
        try {
            parser = new DynamicDifferentialLogicParser(tokens);
            // Remove default console error listener and add a custom one
            parser.removeErrorListeners();
            parser.addErrorListener(parserErrorListener);
        } catch (Exception e) {
            log.error("Parser initialization failed: {}", e.getMessage());
            throw e;
        }
        return parser;
    }

    private ParseTree creatingParseTree(String input) {
        DynamicDifferentialLogicLexer lexer = creatingLexer(input);
        CommonTokenStream tokens = creatingToken(lexer);
        DynamicDifferentialLogicParser parser = creatingParser(tokens);

        // ParseTree reference to parse the input
        ParseTree tree;
        try {
            tree = parser.dlProgram();
            CountingErrorsListener lexerErrorListener = (CountingErrorsListener) lexer.getErrorListeners().getFirst();
            CountingErrorsListener parserErrorListener = (CountingErrorsListener) parser.getErrorListeners().getFirst();
            this.lexerErrorCount = lexerErrorListener.getErrorCount();
            this.parserErrorCount = parserErrorListener.getErrorCount();

            log.info("Lexing completed with {} lexer errors.", this.lexerErrorCount);
            log.info("Parsing completed with {} parser errors.", this.parserErrorCount);
        } catch (Exception e) {
            log.error("Parsing failed: {}", e.getMessage());
            throw e;
        }
        return tree;
    }

    public String generateASTFromDLInput(String input) throws Exception {
        ParseTree tree = creatingParseTree(input);
        // Only generate AST if there are no syntax errors
        boolean hasLexerErrors = false;
        if (this.lexerErrorCount == 0 && this.parserErrorCount == 0) {
            // Walk the parse tree with the help of listener
            ParseTreeWalker walker = new ParseTreeWalker();
            this.listener = new DlAstListener();
            try {
                walker.walk(listener, tree);
            } catch (Exception e) {
                log.error("Error during AST Generation: {}", e.getMessage());
                e.printStackTrace();
                throw e;
            }
            return null;
        } else {
            StringBuilder message = new StringBuilder("No AST generated due to ");
            if(this.lexerErrorCount > 0) {
                hasLexerErrors = true;
                message.append("lexer errors");
            }
            if(this.parserErrorCount > 0) {
                if(hasLexerErrors)
                    message.append(" and ");
                message.append("parser errors");
            }
            message.append(".");
            log.info("{}", message);
            return message.toString();
        }
    }
}