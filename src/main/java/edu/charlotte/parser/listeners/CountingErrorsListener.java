package edu.charlotte.parser.listeners;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class CountingErrorsListener extends BaseErrorListener {
    private int errorCount = 0;
    private final List<String> errorMessages = new ArrayList<>();
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        String errorMsg = (recognizer instanceof Lexer ? "Lexer" : "Parser") +
                " error at line " + line + ":" + charPositionInLine + " - " + msg;
        errorMessages.add(errorMsg);
        log.error("{}", errorMsg);
        errorCount++;
    }
}
