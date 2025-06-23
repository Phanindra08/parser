package edu.charlotte.parser.parser_for_grammars;

import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicLexer;
import edu.charlotte.parser.antlr4_parser.grammar.dynamic_differential_logic.DynamicDifferentialLogicParser;
import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.utils.Constants;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class GenerateASTForDL extends AbstractASTGenerator<DynamicDifferentialLogicLexer,
        DynamicDifferentialLogicParser, DlAstListener> {

    @Override
    protected DynamicDifferentialLogicLexer createLexerInstance(CharStream input) {
        return new DynamicDifferentialLogicLexer(input);
    }

    @Override
    protected DynamicDifferentialLogicParser createParserInstance(CommonTokenStream tokens) {
        return new DynamicDifferentialLogicParser(tokens);
    }

    @Override
    protected ParseTree invokeTopLevelParseRule(DynamicDifferentialLogicParser parser) {
        return parser.dlProgram();
    }

    @Override
    protected DlAstListener createAstListenerInstance() {
        return new DlAstListener();
    }

    @Override
    public String getTypeName() {
        return Constants.DIFFERENTIAL_DYNAMIC_LOGIC;
    }
}