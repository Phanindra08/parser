package edu.charlotte.parser.combining.dl;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.combining.AbstractTwoFilesCombiningProcess;
import edu.charlotte.parser.combining.GenerateCombinedOutput;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYmaeraXConverter;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class DlTwoFilesCombiningProcess extends AbstractTwoFilesCombiningProcess<GenerateAstForDl, DlAstListener> {

    private final DlTwoFileCombining dlTwoFileCombining;
    private final DlToKeYmaeraXConverter dlToKeYmaeraXConverter;

    public DlTwoFilesCombiningProcess(GenerateAstForDl generateAstForDlInput1, GenerateAstForDl generateAstForDlInput2,
                                      GenerateCombinedOutput generateCombinedOutput, DlTwoFileCombining dlTwoFileCombining,
                                      DlToKeYmaeraXConverter dlToKeYmaeraXConverter) {
        super(generateAstForDlInput1, generateAstForDlInput2, Constants.DIFFERENTIAL_DYNAMIC_LOGIC, generateCombinedOutput);
        this.dlTwoFileCombining = dlTwoFileCombining;
        this.dlToKeYmaeraXConverter = dlToKeYmaeraXConverter;
        log.debug("DlTwoFilesCombiningProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving Ast root.");
        return listener.getAst();
    }

    @Override
    protected String performCombiningTwoInputs(AstNode astRootForInput1,
                                               AstNode astRootForInput2, float constantValue, Map<String, String> identifiersConversionMappingForInput2) {
        Objects.requireNonNull(astRootForInput1, "Ast root cannot be null while combining two DL inputs.");
        Objects.requireNonNull(astRootForInput2, "Ast root cannot be null while combining two DL inputs.");
        AstNode astRoot = this.dlTwoFileCombining.combiningTwoDlFiles(astRootForInput1, astRootForInput2, constantValue, identifiersConversionMappingForInput2);

        log.debug("There are {} identifiers after combining two DL files.", this.identifiers.size());
        return this.dlToKeYmaeraXConverter.convertDlToKeYmaeraX(astRoot);
    }
}