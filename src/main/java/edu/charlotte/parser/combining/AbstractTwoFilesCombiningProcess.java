package edu.charlotte.parser.combining;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.dto.MultipleFileContentDTO;
import edu.charlotte.parser.grammars.AbstractAstGenerator;
import edu.charlotte.parser.listeners.ast.DlAstListener;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@StepScope
@Slf4j
public abstract class AbstractTwoFilesCombiningProcess<
        TGenerator extends AbstractAstGenerator<?, ?, TListener>,
        TListener extends ParseTreeListener>
        implements ItemProcessor<MultipleFileContentDTO, String>, StepExecutionListener {

    private final TGenerator astGeneratorForInput1;
    private final TGenerator astGeneratorForInput2;
    private final String processorName;
    private final GenerateCombinedOutput generateCombinedOutput;
    protected final Set<String> identifiers;

    public AbstractTwoFilesCombiningProcess(TGenerator astGeneratorForInput1, TGenerator astGeneratorForInput2, String processorName,
                                            GenerateCombinedOutput generateCombinedOutput) {
        this.astGeneratorForInput1 = Objects.requireNonNull(astGeneratorForInput1, "AST Generator cannot be null");
        this.astGeneratorForInput2 = Objects.requireNonNull(astGeneratorForInput2, "AST Generator cannot be null");
        this.processorName = Objects.requireNonNull(processorName, "Processor name cannot be null");
        this.generateCombinedOutput = Objects.requireNonNull(generateCombinedOutput, "Combined Output generator cannot be null");
        this.identifiers = new HashSet<>();
        log.info("'{}' is initialized.", this.getDisplayName());
    }

    protected String getDisplayName() {
        return String.format("%s", Constants.MESSAGE_TO_COMBINE_TWO_INPUTS, this.processorName);
    }

    // Abstract methods to be implemented by subclasses
    protected abstract AstNode getAstRootFromListener(TListener listener);

    protected abstract String performCombiningTwoInputs(AstNode astRootForInput1,
                                                        AstNode astRootForInput2, float constantValue, Map<String, String> identifiersConversionMappingForInput2);

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
        log.debug("Processing the input item for '{}': '{}', '{}'.", this.getDisplayName(), ParserUtils.formatInputForLogging(multipleFileContentDTO.firstFileContent()),
                ParserUtils.formatInputForLogging(multipleFileContentDTO.secondFileContent()));
        try {
            String errorMessageForInput1 = this.astGeneratorForInput1.generateAstFromInput(multipleFileContentDTO.firstFileContent());
            String errorMessageForInput2 = this.astGeneratorForInput2.generateAstFromInput(multipleFileContentDTO.secondFileContent());
            if (errorMessageForInput1 == null && errorMessageForInput2 == null) {
                TListener listenerForInput1 = this.astGeneratorForInput1.getListener();
                TListener listenerForInput2 = this.astGeneratorForInput2.getListener();
                AstNode astRootForInput1 = getAstRootFromListener(listenerForInput1);
                AstNode astRootForInput2 = getAstRootFromListener(listenerForInput2);

                Map<String, String> identifiersConversionMappingForInput2 = null;
                if (listenerForInput1 instanceof DlAstListener && listenerForInput2 instanceof DlAstListener)
                    identifiersConversionMappingForInput2 = loadIdentifiersFromTwoInputs((DlAstListener) listenerForInput1, (DlAstListener) listenerForInput2);

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

                String combinedOutput = performCombiningTwoInputs(astRootForInput1, astRootForInput2, multipleFileContentDTO.constantValue(), identifiersConversionMappingForInput2);

                if (combinedOutput == null) {
                    log.warn("Combining two inputs returned null output for the inputs: {}, {}. Skipping the inputs.",
                            ParserUtils.formatInputForLogging(multipleFileContentDTO.firstFileContent()),
                            ParserUtils.formatInputForLogging(multipleFileContentDTO.secondFileContent()));
                    return null;
                }

                log.debug("Combined {} Output is: {}", this.processorName, combinedOutput);
                return this.generateCombinedOutput.createFileContent(combinedOutput, this.processorName);
            } else {
                this.showErrorMessageWhenAstGenerationFailed(errorMessageForInput1, multipleFileContentDTO.firstFileContent());
                this.showErrorMessageWhenAstGenerationFailed(errorMessageForInput2, multipleFileContentDTO.secondFileContent());
                return null;
            }
        } catch (Exception e) {
            log.error("Error combining two input files with the values: {} and {}. The Error is: {}",
                    ParserUtils.formatInputForLogging(multipleFileContentDTO.firstFileContent()), ParserUtils.formatInputForLogging(multipleFileContentDTO.secondFileContent()),
                    e.getMessage(), e);
            throw new RuntimeException("Error combining two input files due to internal error.", e);
        }
    }

    private Map<String, String> loadIdentifiersFromTwoInputs(DlAstListener listenerForInput1, DlAstListener listenerForInput2) {
        this.identifiers.addAll(listenerForInput1.getIdentifiers());
        Map<String, String> identifiersConversionMappingForInput2 = new HashMap<>();
        Set<String> input2Identifiers = listenerForInput2.getIdentifiers();
        String value;
        for (String identifier : input2Identifiers) {
            if (this.identifiers.contains(identifier)) {
                Pattern pattern = Pattern.compile(Constants.REG_EXP_FOR_NUMBERS_AT_END);
                Matcher matcher = pattern.matcher(identifier);
                if (matcher.find())
                    value = matcher.group(1) + Integer.parseInt(matcher.group(2)) + 1;
                else
                    value = identifier + 1;
                identifiersConversionMappingForInput2.put(identifier, value);
                this.identifiers.add(value);
            } else
                this.identifiers.add(identifier);
        }
        return identifiersConversionMappingForInput2;
    }

    private void showErrorMessageWhenAstGenerationFailed(String errorMessage, String fileContent) {
        if (errorMessage == null) {
            log.warn("AST generation failed for the item: '{}' due to the Error: {}. Skipping the item.",
                    ParserUtils.formatInputForLogging(fileContent), null);
        }
    }
}