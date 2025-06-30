package edu.charlotte.parser.ast.generation;

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

@StepScope
@Slf4j
public abstract class AbstractAstGenerationProcess<TGenerator extends AbstractAstGenerator<?, ?, TListener>,
        TListener extends ParseTreeListener> implements ItemProcessor<String, String>, StepExecutionListener {

    private final TGenerator astGenerator;
    private final String processorName;
    public AbstractAstGenerationProcess(TGenerator astGenerator) {
        this.astGenerator = astGenerator;
        this.processorName = this.astGenerator.getTypeName();
        log.info("'{}' is initialized.", getDisplayName());
    }

    // Abstract methods to be implemented by subclasses
    protected abstract AstNode getAstRootFromListener(TListener listener);

    private String getDisplayName() {
        return this.processorName + Constants.AST_GENERATION_PROCESS_SUFFIX;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.debug("Before step for the '{}'. Step Name is '{}'.", getDisplayName(), stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("After step for the '{}'. Step Name is '{}', Status is '{}'.",
                getDisplayName(), stepExecution.getStepName(), stepExecution.getExitStatus().getExitCode());
        return stepExecution.getExitStatus();
    }

    @Override
    public String process(@NonNull String item) {
        log.debug("Processing the input item: {}.", ParserUtils.formatInputForLogging(item));
        StringBuilder astOutputTree = new StringBuilder("Generated AST is:").append("\n");
        String errorMessage = this.astGenerator.generateAstFromInput(item);
        try {
            if(errorMessage == null) {
                TListener listener = this.astGenerator.getListener();
                AstNode astRoot = getAstRootFromListener(listener);
                if(astRoot != null) {
                    astRoot.generateAstTree("", true, astOutputTree);
                    log.debug("AST is generated successfully for the {}.", this.processorName);
                } else {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }
            } else
                return errorMessage;
        } catch (Exception e) {
            log.error("Error during AST generation for the item: {}. The Error is: {}",
                    ParserUtils.formatInputForLogging(item), e.getMessage(), e);
            throw new RuntimeException("Error during AST generation due to internal error.", e);
        }
        return astOutputTree.toString();
    }
}