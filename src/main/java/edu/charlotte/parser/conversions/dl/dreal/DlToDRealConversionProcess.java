package edu.charlotte.parser.conversions.dl.dreal;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.conversions.common.GenerateDRealOutput;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.utils.Constants;
import edu.charlotte.parser.utils.ParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class DlToDRealConversionProcess implements ItemProcessor<String, String>, StepExecutionListener {

    private final DlToDRealConverter dlToDRealConverter;
    private final GenerateAstForDl generateAstForDl;
    private final String processorName;
    private final GenerateDRealOutput generateDRealOutput;
    protected final Set<String> identifiers;

    public DlToDRealConversionProcess(GenerateAstForDl generateAstForDl, GenerateDRealOutput generateDRealOutput,
                                      DlToDRealConverter dlToDRealConverter) {
        this.generateAstForDl = Objects.requireNonNull(generateAstForDl, "AST Generator cannot be null");
        this.processorName = Constants.DIFFERENTIAL_DYNAMIC_LOGIC;
        this.dlToDRealConverter = dlToDRealConverter;
        this.generateDRealOutput = Objects.requireNonNull(generateDRealOutput, "DReal Output generator cannot be null");
        this.identifiers = new HashSet<>();
        log.debug("DlToDRealConversionProcess is initialized.");
    }

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
            String errorMessage = this.generateAstForDl.generateAstFromInput(item);
            if (errorMessage == null) {
                DlAstListener listener = this.generateAstForDl.getListener();
                AstNode astRoot = getAstRootFromListener(listener);

                if (astRoot == null) {
                    String nullAstError = "AST generation completed without any explicit errors, but returned a null AST root. Hence, Cannot convert to DReal output.";
                    log.error("{}", nullAstError);
                    return nullAstError;
                }
                String dRealOutput = performDRealConversionAndCollectIdentifiers(astRoot, getIdentifiersDataFromListener(listener));

                if (dRealOutput == null) {
                    log.warn("dReal conversion returned null output for the item: {}. Skipping the item.",
                            ParserUtils.formatInputForLogging(item));
                    return null;
                }

                log.debug("dReal Output is: {}", dRealOutput);
                return this.generateDRealOutput.createFileContent(this.generateAstForDl.getTypeName(),
                        this.identifiers, dRealOutput);
            } else {
                log.warn("AST generation failed for item: '{}' due to Error: {}. Skipping the item.",
                        ParserUtils.formatInputForLogging(item), errorMessage);
                return null;
            }
        } catch (Exception e) {
            log.error("Error during dReal conversion process for the item: {}. The Error is: {}",
                    ParserUtils.formatInputForLogging(item), e.getMessage(), e);
            throw new RuntimeException("Error during dReal conversion due to internal error.", e);
        }
    }

    protected AstNode getAstRootFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving Ast root.");
        return listener.getAst();
    }

    protected Set<String> getIdentifiersDataFromListener(DlAstListener listener) {
        Objects.requireNonNull(listener, "DlAstListener cannot be null when retrieving identifiers data.");
        return listener.getIdentifiers();
    }

    protected String performDRealConversionAndCollectIdentifiers(AstNode astRoot, Set<String> identifierData) {
        Objects.requireNonNull(astRoot, "Ast root cannot be null for DL to DReal conversion.");
        Objects.requireNonNull(identifierData, "Identifiers data cannot be null for DL to DReal conversion.");

        this.identifiers.addAll(identifierData);
        log.debug("There are {} identifiers in the DL program.", this.identifiers.size());
        return this.dlToDRealConverter.convertDlToDReal(astRoot);
    }

    protected String getDisplayName() {
        return String.format("%s%s", this.processorName, Constants.D_REAL_OUTPUT_CONVERSION_SUFFIX);
    }
}