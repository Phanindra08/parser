package edu.charlotte.parser.listeners;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

@Getter
@Slf4j
public class CountingErrorsListener extends BaseErrorListener {
    private int errorCount = 0;

    public CountingErrorsListener() {
        log.debug("Instance of CountingErrorsListener is created.");
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        String errorSource = (recognizer instanceof Lexer) ? "Lexer" : "Parser";
        String errorMsg = String.format("%s error at line %d: %d - %s", errorSource, line, charPositionInLine, msg);
        if(e != null)
            log.error("{}", errorMsg, e);
        else
            log.error("{}", errorMsg);
        errorCount++;
    }
}