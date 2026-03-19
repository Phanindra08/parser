package edu.charlotte.parser.combining.dl;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.combining.AbstractTwoFilesCombiningProcess;
import edu.charlotte.parser.combining.GenerateCombinedOutput;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class DlTwoFilesCombiningProcess extends AbstractTwoFilesCombiningProcess<GenerateAstForDl, DlAstListener> {

    private final DlTwoFileCombining dlTwoFileCombining;

    public DlTwoFilesCombiningProcess(GenerateAstForDl generateAstForDlPreConditionInput, GenerateAstForDl generateAstForDlPostConditionInput,
                                      GenerateAstForDl generateAstForDlInput1, GenerateAstForDl generateAstForDlInput2,
                                      GenerateCombinedOutput generateCombinedOutput, DlTwoFileCombining dlTwoFileCombining) {
        super(generateAstForDlPreConditionInput, generateAstForDlPostConditionInput, generateAstForDlInput1,
                generateAstForDlInput2, Constants.DIFFERENTIAL_DYNAMIC_LOGIC, generateCombinedOutput);
        this.dlTwoFileCombining = dlTwoFileCombining;
        log.debug("DlTwoFilesCombiningProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving Ast root.");
        return listener.getAst();
    }

    @Override
    protected String performCombiningTwoInputs(AstNode astRootForPreConditionInput, AstNode astRootForPostConditionInput, AstNode astRootForInput1,
                                               AstNode astRootForInput2, int constantValue) {
        Objects.requireNonNull(astRootForPreConditionInput, "Ast root cannot be null for pre condition to combine two DL inputs.");
        Objects.requireNonNull(astRootForPostConditionInput, "Ast root cannot be null for post condition to combine two DL inputs.");
        Objects.requireNonNull(astRootForInput1, "Ast root cannot be null while combining two DL inputs.");
        Objects.requireNonNull(astRootForInput2, "Ast root cannot be null while combining two DL inputs.");
        return this.dlTwoFileCombining.combiningTwoDlFiles(astRootForPreConditionInput, astRootForPostConditionInput, astRootForInput1, astRootForInput2, constantValue);
    }
}