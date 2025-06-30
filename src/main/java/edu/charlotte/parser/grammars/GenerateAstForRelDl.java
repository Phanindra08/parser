package edu.charlotte.parser.grammars;

import edu.charlotte.parser.listeners.ast.RelDlAstListener;
import edu.charlotte.parser.relational_dynamic_logic.RelationalDynamicLogicLexer;
import edu.charlotte.parser.relational_dynamic_logic.RelationalDynamicLogicParser;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

@Slf4j
public class GenerateAstForRelDl extends AbstractAstGenerator<RelationalDynamicLogicLexer,
        RelationalDynamicLogicParser, RelDlAstListener> {

    private final boolean hasKeYmaeraXConversion;

    public GenerateAstForRelDl(boolean hasKeYmaeraXConversion) {
        super();
        this.hasKeYmaeraXConversion = hasKeYmaeraXConversion;
    }

    @Override
    protected RelationalDynamicLogicLexer createLexerInstance(CharStream input) {
        return new RelationalDynamicLogicLexer(input);
    }

    @Override
    protected RelationalDynamicLogicParser createParserInstance(CommonTokenStream tokens) {
        return new RelationalDynamicLogicParser(tokens);
    }

    @Override
    protected ParseTree invokeTopLevelParseRule(RelationalDynamicLogicParser parser) {
        return parser.relDlProgram();
    }

    @Override
    protected RelDlAstListener createAstListenerInstance() {
        return new RelDlAstListener(this.hasKeYmaeraXConversion);
    }

    @Override
    public String getTypeName() {
        return Constants.RELATIONAL_DYNAMIC_LOGIC;
    }
}