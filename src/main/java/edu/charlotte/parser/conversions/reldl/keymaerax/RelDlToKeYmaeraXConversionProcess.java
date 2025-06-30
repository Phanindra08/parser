package edu.charlotte.parser.conversions.reldl.keymaerax;

import edu.charlotte.parser.listeners.ast.RelDlAstListener;
import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.conversions.common.AbstractKeYmaeraXConversionProcess;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYmaeraXConverter;
import edu.charlotte.parser.conversions.common.GenerateKeYmaeraXOutput;
import edu.charlotte.parser.conversions.reldl.dl.RelDlToDlConverter;
import edu.charlotte.parser.grammars.GenerateAstForRelDl;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class RelDlToKeYmaeraXConversionProcess extends AbstractKeYmaeraXConversionProcess<GenerateAstForRelDl, RelDlAstListener, Map<Character, Set<String>>> {

    private final DlToKeYmaeraXConverter dlToKeYmaeraXConverter;

    public RelDlToKeYmaeraXConversionProcess(GenerateAstForRelDl generateAstForRelDl, GenerateKeYmaeraXOutput generateKeYmaeraXOutput,
                                             DlToKeYmaeraXConverter dlToKeYmaeraXConverter) {
        super(generateAstForRelDl, Constants.RELATIONAL_DYNAMIC_LOGIC, generateKeYmaeraXOutput);
        this.dlToKeYmaeraXConverter = dlToKeYmaeraXConverter;
        log.debug("RelDlToKeYmaeraXConversionProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(RelDlAstListener listener) {
        Objects.requireNonNull(listener, "RelDlAstListener cannot be null when retrieving Ast root.");
        return listener.getAst();
    }

    @Override
    protected Map<Character, Set<String>> getIdentifiersDataFromListener(RelDlAstListener listener) {
        Objects.requireNonNull(listener, "RelDlAstListener cannot be null when retrieving identifiers data.");
        return listener.getIdentifiers();
    }

    @Override
    protected String performKeYmaeraXConversionAndCollectIdentifiers(AstNode astRoot, Map<Character, Set<String>> identifierData) {
        Objects.requireNonNull(astRoot, "Ast root cannot be null for RelDL to KeYmaeraX conversion.");
        Objects.requireNonNull(identifierData, "Identifiers data cannot be null for RelDL to KeYmaeraX conversion.");

        RelDlToDlConverter relDlToDlConverter = new RelDlToDlConverter(identifierData);
        relDlToDlConverter.convertRelDlToDl(astRoot);

        this.identifiers.addAll(relDlToDlConverter.getIdentifiers());
        log.debug("There are {} identifiers after RelDL to DL conversion.", relDlToDlConverter.getIdentifiers().size());

        return this.dlToKeYmaeraXConverter.convertDlToKeYmaeraX(astRoot);
    }
}