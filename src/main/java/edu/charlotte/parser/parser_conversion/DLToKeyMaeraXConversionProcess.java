package edu.charlotte.parser.parser_conversion;

import edu.charlotte.parser.parser_for_grammars.GenerateASTForDL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

@StepScope
@Slf4j
public class DLToKeyMaeraXConversionProcess implements ItemProcessor<String, String>, StepExecutionListener {

    @Autowired
    GenerateASTForDL generateASTForDL;

    @Autowired
    GenerateKeYMaeraXOutput generateKeYMaeraXOutput;

    @Override
    public void beforeStep(StepExecution stepExecution) {}

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public String process(String item) throws Exception {
        log.debug("The input is: {}.", item);
        DLToKeYMaeraXConverter converter;
        String errorMessage = this.generateASTForDL.generateASTFromDLInput(item);
        try {
            if(errorMessage == null) {
                converter = new DLToKeYMaeraXConverter(this.generateASTForDL.getListener().getAST());
                log.debug("KeYmaera X Output is: {}", converter.getKeYMaeraXOutput());
            } else
                return errorMessage;
        } catch (Exception e) {
            log.error("Error during KeYmaera X conversion: {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return generateKeYMaeraXOutput.createFileContent(this.generateASTForDL.getListener().getIdentifiers(), converter.getKeYMaeraXOutput());
    }
}