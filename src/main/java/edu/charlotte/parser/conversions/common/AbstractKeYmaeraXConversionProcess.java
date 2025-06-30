package edu.charlotte.parser.conversions.common;

import edu.charlotte.parser.ast.nodes.AstNode;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@StepScope
@Slf4j
public abstract class AbstractKeYmaeraXConversionProcess<
        TGenerator extends AbstractAstGenerator<?, ?, TListener>,
        TListener extends ParseTreeListener, TIdentifiersData>
        implements ItemProcessor<String, String>, StepExecutionListener {

    private final TGenerator astGenerator;
    private final String processorName;
    private final GenerateKeYmaeraXOutput generateKeYmaeraXOutput;
    protected final Set<String> identifiers;

    public AbstractKeYmaeraXConversionProcess(TGenerator astGenerator, String processorName, GenerateKeYmaeraXOutput generateKeYmaeraXOutput) {
        this.astGenerator = Objects.requireNonNull(astGenerator, "AST Generator cannot be null");
        this.processorName = Objects.requireNonNull(processorName, "Processor name cannot be null");
        this.generateKeYmaeraXOutput = Objects.requireNonNull(generateKeYmaeraXOutput, "KeYmaeraX Output generator cannot be null");
        this.identifiers = new HashSet<>();
        log.info("'{}' is initialized.", this.getDisplayName());
    }

    protected String getDisplayName() {
        return String.format("%s%s", this.processorName, Constants.KEYMAERAX_OUTPUT_CONVERSION_SUFFIX);
    }

    // Abstract methods to be implemented by subclasses
    protected abstract AstNode getAstRootFromListener(TListener listener);
    protected abstract TIdentifiersData getIdentifiersDataFromListener(TListener listener);
    protected abstract String performKeYmaeraXConversionAndCollectIdentifiers(AstNode astRoot, TIdentifiersData identifierData);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.identifiers.clear();
        log.debug("Before step for the '{}'. Step Name: '{}'. Identifiers have been cleared.", getDisplayName(), stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("After step for the '{}'. Step Name is '{}', Status is '{}'.",
                getDisplayName(), stepExecution.getStepName(), stepExecution.getExitStatus().getExitCode());
        return stepExecution.getExitStatus();
    }

    @Override
    public String process(@NonNull String item) {
        log.debug("Processing the input item for '{}': {}.", this.getDisplayName(), ParserUtils.formatInputForLogging(item));
        try {
            String errorMessage = this.astGenerator.generateAstFromInput(item);
            if (errorMessage == null) {
                TListener listener = this.astGenerator.getListener();
                AstNode astRoot = getAstRootFromListener(listener);

                if (astRoot == null) {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root. Hence, Cannot convert to KeYmaeraX output.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }
                String keYmaeraXOutput = performKeYmaeraXConversionAndCollectIdentifiers(astRoot, getIdentifiersDataFromListener(listener));

                if (keYmaeraXOutput == null) {
                    log.warn("KeYmaeraX conversion returned null output for the item: {}. Skipping the item.",
                            ParserUtils.formatInputForLogging(item));
                    return null;
                }

                log.debug("KeYmaera X Output is: {}", keYmaeraXOutput);
                return this.generateKeYmaeraXOutput.createFileContent(this.astGenerator.getTypeName(),
                        this.identifiers, keYmaeraXOutput);
            } else {
                log.warn("AST generation failed for item: '{}' due to Error: {}. Skipping the item.",
                        ParserUtils.formatInputForLogging(item), errorMessage);
                return null;
            }
        } catch (Exception e) {
            log.error("Error during KeYmaeraX conversion process for the item: {}. The Error is: {}",
                    ParserUtils.formatInputForLogging(item), e.getMessage(), e);
            throw new RuntimeException("Error during KeYmaeraX conversion due to internal error.", e);
        }
    }
}