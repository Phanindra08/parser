package edu.charlotte.parser.combining;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.dto.MultipleFileContentDTO;
import edu.charlotte.parser.exceptions.InvalidInputException;
import edu.charlotte.parser.grammars.AbstractAstGenerator;
import edu.charlotte.parser.utils.Constants;
import edu.charlotte.parser.utils.ParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.util.Objects;

@StepScope
@Slf4j
public abstract class AbstractTwoFilesCombiningProcess<
        TGenerator extends AbstractAstGenerator<?, ?, TListener>,
        TListener extends ParseTreeListener>
        implements ItemProcessor<MultipleFileContentDTO, String>, StepExecutionListener {

    private final TGenerator astGeneratorForPreConditionInput;
    private final TGenerator astGeneratorForPostConditionInput;
    private final TGenerator astGeneratorForInput1;
    private final TGenerator astGeneratorForInput2;
    private final String processorName;
    private final GenerateCombinedOutput generateCombinedOutput;

    public AbstractTwoFilesCombiningProcess(TGenerator generateAstForDlPreConditionInput, TGenerator generateAstForDlPostConditionInput, TGenerator astGeneratorForInput1,
                                            TGenerator astGeneratorForInput2, String processorName, GenerateCombinedOutput generateCombinedOutput) {
        this.astGeneratorForPreConditionInput = Objects.requireNonNull(generateAstForDlPreConditionInput, "AST Generator cannot be null for precondition input");
        this.astGeneratorForPostConditionInput = Objects.requireNonNull(generateAstForDlPostConditionInput, "AST Generator cannot be null for postcondition input");
        this.astGeneratorForInput1 = Objects.requireNonNull(astGeneratorForInput1, "AST Generator cannot be null");
        this.astGeneratorForInput2 = Objects.requireNonNull(astGeneratorForInput2, "AST Generator cannot be null");
        this.processorName = Objects.requireNonNull(processorName, "Processor name cannot be null");
        this.generateCombinedOutput = Objects.requireNonNull(generateCombinedOutput, "Combined Output generator cannot be null");
        log.info("'{}' is initialized.", this.getDisplayName());
    }

    protected String getDisplayName() {
        return String.format("%s", Constants.MESSAGE_TO_COMBINE_TWO_INPUTS, this.processorName);
    }

    // Abstract methods to be implemented by subclasses
    protected abstract AstNode getAstRootFromListener(TListener listener);

    protected abstract String performCombiningTwoInputs(AstNode astRootForPreConditionInput, AstNode astRootForPostConditionInput, AstNode astRootForInput1,
                                                        AstNode astRootForInput2, float constantValue);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.debug("Before step for the '{}'. Step Name: '{}'.", getDisplayName(), stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("After step for the '{}'. Step Name is '{}', Status is '{}'.",
                getDisplayName(), stepExecution.getStepName(), stepExecution.getExitStatus().getExitCode());
        return stepExecution.getExitStatus();
    }

    @Override
    public String process(@NonNull MultipleFileContentDTO multipleFileContentDTO) {
        log.debug("Processing the input item for '{}': {}, {}.", this.getDisplayName(), ParserUtils.formatInputForLogging(multipleFileContentDTO.firstFileContent()),
                ParserUtils.formatInputForLogging(multipleFileContentDTO.secondFileContent()));
        try {
            String[] conditions = multipleFileContentDTO.conditionsInputFileContent().toLowerCase().split(Constants.POST_CONDITION.toLowerCase());
            boolean isConditionsEmpty = ParserUtils.checkingArrayInputIsNotEmpty(conditions, 2);
            if (!isConditionsEmpty)
                throw new InvalidInputException("Invalid Pre or Post Condition value.");
            conditions[0] = conditions[0].substring(conditions[0].indexOf(Constants.SEMI_COLON) + 1);
            String errorMessageForPreConditionInput = this.astGeneratorForPreConditionInput.generateAstFromInput(conditions[0]);
            String errorMessageForPostConditionInput = this.astGeneratorForPostConditionInput.generateAstFromInput(conditions[1]);
            String errorMessageForInput1 = this.astGeneratorForInput1.generateAstFromInput(multipleFileContentDTO.firstFileContent());
            String errorMessageForInput2 = this.astGeneratorForInput2.generateAstFromInput(multipleFileContentDTO.secondFileContent());
            if (errorMessageForPreConditionInput == null && errorMessageForPostConditionInput == null &&
                    errorMessageForInput1 == null && errorMessageForInput2 == null) {
                TListener listenerForPreConditionInput = this.astGeneratorForPreConditionInput.getListener();
                TListener listenerForPostConditionInput = this.astGeneratorForPostConditionInput.getListener();
                TListener listenerForInput1 = this.astGeneratorForInput1.getListener();
                TListener listenerForInput2 = this.astGeneratorForInput2.getListener();
                AstNode astRootForPreConditionInput = getAstRootFromListener(listenerForPreConditionInput);
                AstNode astRootForPostConditionInput = getAstRootFromListener(listenerForPostConditionInput);
                AstNode astRootForInput1 = getAstRootFromListener(listenerForInput1);
                AstNode astRootForInput2 = getAstRootFromListener(listenerForInput2);

                if (astRootForPreConditionInput == null) {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root for Pre Condition Input. " +
                            "Hence, Cannot combine the two Inputs.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }

                if (astRootForPostConditionInput == null) {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root for Post Condition Input. " +
                            "Hence, Cannot combine the two Inputs.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }

                if (astRootForInput1 == null) {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root for Input 1. " +
                            "Hence, Cannot combine the two Inputs.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }

                if (astRootForInput2 == null) {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root for Input 2. Hence, Cannot combine the two Inputs.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }

                String combinedOutput = performCombiningTwoInputs(astRootForPreConditionInput, astRootForPostConditionInput,
                        astRootForInput1, astRootForInput2, multipleFileContentDTO.constantValue());

                if (combinedOutput == null) {
                    log.warn("Combining two inputs returned null output for the inputs: {}, {}. Skipping the inputs.",
                            ParserUtils.formatInputForLogging(multipleFileContentDTO.firstFileContent()),
                            ParserUtils.formatInputForLogging(multipleFileContentDTO.secondFileContent()));
                    return null;
                }

                log.debug("Combined {} Output is: {}", this.processorName, combinedOutput);
                return this.generateCombinedOutput.createFileContent(combinedOutput, this.processorName);
            } else {
                this.showErrorMessageWhenAstGenerationFailed(errorMessageForPreConditionInput, conditions[0]);
                this.showErrorMessageWhenAstGenerationFailed(errorMessageForPostConditionInput, conditions[1]);
                this.showErrorMessageWhenAstGenerationFailed(errorMessageForInput1, multipleFileContentDTO.firstFileContent());
                this.showErrorMessageWhenAstGenerationFailed(errorMessageForInput2, multipleFileContentDTO.secondFileContent());
                return null;
            }
        } catch (InvalidInputException e) {
            log.error("Invalid Pre or Post Condition value: {}", multipleFileContentDTO.conditionsInputFileContent(), e);
            throw new InvalidInputException("Invalid Pre or Post Condition value.", e);
        } catch (Exception e) {
            log.error("Error combining two input files with the values: {} and {}. The Error is: {}",
                    ParserUtils.formatInputForLogging(multipleFileContentDTO.firstFileContent()), ParserUtils.formatInputForLogging(multipleFileContentDTO.secondFileContent()),
                    e.getMessage(), e);
            throw new RuntimeException("Error combining two input files due to internal error.", e);
        }
    }

    private void showErrorMessageWhenAstGenerationFailed(String errorMessage, String fileContent) {
        if (errorMessage == null) {
            log.warn("AST generation failed for the item: '{}' due to the Error: {}. Skipping the item.",
                    ParserUtils.formatInputForLogging(fileContent), null);
        }
    }
}