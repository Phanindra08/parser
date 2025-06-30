package edu.charlotte.parser.conversions.dl.keymaerax;

import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.conversions.common.AbstractKeYmaeraXConversionProcess;
import edu.charlotte.parser.conversions.common.GenerateKeYmaeraXOutput;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

@Slf4j
public class DlToKeYmaeraXConversionProcess extends AbstractKeYmaeraXConversionProcess<GenerateAstForDl, DlAstListener, Set<String>> {

    private final DlToKeYmaeraXConverter dlToKeYmaeraXConverter;

    public DlToKeYmaeraXConversionProcess(GenerateAstForDl generateAstForDl, GenerateKeYmaeraXOutput generateKeYmaeraXOutput,
                                          DlToKeYmaeraXConverter dlToKeYmaeraXConverter) {
        super(generateAstForDl, Constants.DIFFERENTIAL_DYNAMIC_LOGIC, generateKeYmaeraXOutput);
        this.dlToKeYmaeraXConverter = dlToKeYmaeraXConverter;
        log.debug("DlToKeYmaeraXConversionProcess is initialized.");
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
    protected String performKeYmaeraXConversionAndCollectIdentifiers(AstNode astRoot, Set<String> identifierData) {
        Objects.requireNonNull(astRoot, "AST root cannot be null for DL to KeYmaeraX conversion.");
        Objects.requireNonNull(identifierData, "Identifiers data cannot be null for DL to KeYmaeraX conversion.");

        this.identifiers.addAll(identifierData);
        log.debug("There are {} identifiers in the DL program.", this.identifiers.size());
        return this.dlToKeYmaeraXConverter.convertDlToKeYmaeraX(astRoot);
    }
}