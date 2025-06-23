package edu.charlotte.parser.parser_conversions.dl_to_keymaerax_conversion;

import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.parser_conversions.AbstractKeYMaeraXConversionProcess;
import edu.charlotte.parser.parser_conversions.GenerateKeYMaeraXOutput;
import edu.charlotte.parser.parser_for_grammars.GenerateASTForDL;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

@Slf4j
public class DLToKeyMaeraXConversionProcess extends AbstractKeYMaeraXConversionProcess<GenerateASTForDL, DlAstListener, Set<String>> {

    private final DLToKeYMaeraXConverter dlToKeYMaeraXConverter;

    public DLToKeyMaeraXConversionProcess(GenerateASTForDL generateASTForDL, GenerateKeYMaeraXOutput generateKeYMaeraXOutput,
                                          DLToKeYMaeraXConverter dlToKeYMaeraXConverter) {
        super(generateASTForDL, Constants.DIFFERENTIAL_DYNAMIC_LOGIC, generateKeYMaeraXOutput);
        this.dlToKeYMaeraXConverter = dlToKeYMaeraXConverter;
        log.debug("DLToKeyMaeraXConversionProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving AST root.");
        return listener.getAST();
    }

    @Override
    protected Set<String> getIdentifiersDataFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving identifiers data.");
        return listener.getIdentifiers();
    }

    @Override
    protected String performKeYMaeraXConversionAndCollectIdentifiers(AstNode astRoot, Set<String> identifierData) {
        Objects.requireNonNull(astRoot, "AST root cannot be null for DL to KeYMaeraX conversion.");
        Objects.requireNonNull(identifierData, "Identifiers data cannot be null for DL to KeYMaeraX conversion.");

        this.identifiers.addAll(identifierData);
        log.debug("There are {} identifiers in the DL program.", this.identifiers.size());
        return this.dlToKeYMaeraXConverter.convertDLToKeYMaeraX(astRoot);
    }
}