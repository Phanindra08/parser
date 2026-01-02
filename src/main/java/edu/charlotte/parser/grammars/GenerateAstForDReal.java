package edu.charlotte.parser.grammars;

import edu.charlotte.parser.d_real.DRealLexer;
import edu.charlotte.parser.d_real.DRealParser;
import edu.charlotte.parser.listeners.ast.DRealAstListener;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

@Slf4j
public class GenerateAstForDReal extends AbstractAstGenerator<DRealLexer,
        DRealParser, DRealAstListener> {

    public GenerateAstForDReal() {
        super();
    }

    @Override
    protected DRealLexer createLexerInstance(CharStream input) {
        return new DRealLexer(input);
    }

    @Override
    protected DRealParser createParserInstance(CommonTokenStream tokens) {
        return new DRealParser(tokens);
    }

    @Override
    protected ParseTree invokeTopLevelParseRule(DRealParser parser) {
        return parser.dRealProgram();
    }

    @Override
    protected DRealAstListener createAstListenerInstance() {
        return new DRealAstListener();
    }

    @Override
    public String getTypeName() {
        return Constants.D_REAL;
    }
}