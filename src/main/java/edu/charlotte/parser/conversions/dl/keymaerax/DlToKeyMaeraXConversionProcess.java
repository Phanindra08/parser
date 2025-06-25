package edu.charlotte.parser.conversions.dl.keymaerax;

import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.conversions.common.AbstractKeYMaeraXConversionProcess;
import edu.charlotte.parser.conversions.common.GenerateKeYMaeraXOutput;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

@Slf4j
public class DlToKeyMaeraXConversionProcess extends AbstractKeYMaeraXConversionProcess<GenerateAstForDl, DlAstListener, Set<String>> {

    private final DlToKeYMaeraXConverter dlToKeYMaeraXConverter;

    public DlToKeyMaeraXConversionProcess(GenerateAstForDl generateAstForDl, GenerateKeYMaeraXOutput generateKeYMaeraXOutput,
                                          DlToKeYMaeraXConverter dlToKeYMaeraXConverter) {
        super(generateAstForDl, Constants.DIFFERENTIAL_DYNAMIC_LOGIC, generateKeYMaeraXOutput);
        this.dlToKeYMaeraXConverter = dlToKeYMaeraXConverter;
        log.debug("DlToKeyMaeraXConversionProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving AST root.");
        return listener.getAst();
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
        return this.dlToKeYMaeraXConverter.convertDlToKeYMaeraX(astRoot);
    }
}