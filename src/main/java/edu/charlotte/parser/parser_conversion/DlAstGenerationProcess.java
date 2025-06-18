package edu.charlotte.parser.parser_conversion;

import edu.charlotte.parser.parser_for_grammars.GenerateASTForDL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

@StepScope
@Slf4j
public class DlAstGenerationProcess implements ItemProcessor<String, String>, StepExecutionListener {

    private final GenerateASTForDL generateASTForDL;

    public DlAstGenerationProcess(GenerateASTForDL generateASTForDL) {
        this.generateASTForDL = generateASTForDL;
        log.info("DlAstGenerationProcess initialized.");
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {}

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public String process(String item) {
        log.debug("The input is: {}.", item);
        StringBuilder astOutputTree = new StringBuilder("Generated AST is:").append("\n");
        String errorMessage = this.generateASTForDL.generateASTFromDLInput(item);
        try {
            if(errorMessage == null) {
                this.generateASTForDL.getListener().getAST().generateASTTree("", true, astOutputTree);
                log.debug("AST is generated for Relational Dynamic Logic.");
            } else
                return errorMessage;
        } catch (Exception e) {
            log.error("Error during generating AST Tree output: {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return astOutputTree.toString();
    }
}
