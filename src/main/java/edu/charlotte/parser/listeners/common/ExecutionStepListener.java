package edu.charlotte.parser.listeners.common;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class ExecutionStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        log.info("StepExecutionListener is created");
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        log.debug("BatchStatus: {}", stepExecution.getStatus());
        log.debug("ExitStatus: {}", stepExecution.getExitStatus());
        return stepExecution.getExitStatus();
    }
}