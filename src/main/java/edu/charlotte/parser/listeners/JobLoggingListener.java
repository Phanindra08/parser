package edu.charlotte.parser.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class JobLoggingListener implements JobExecutionListener {

    public JobLoggingListener() {
        log.debug("JobLoggingListener instance is created.");
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job '{}' (ID: {}) started at: {}", jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobId(), jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job '{}' (ID: {}) completed with status: {} at {}",
                jobExecution.getJobInstance().getJobName(), jobExecution.getJobId(),
                jobExecution.getStatus(), jobExecution.getEndTime());

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Job '{}' Failed. Job Exit Description is: {}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getExitStatus().getExitDescription());

            if (!jobExecution.getAllFailureExceptions().isEmpty()) {
                log.error("Job '{}' failure exceptions are:", jobExecution.getJobInstance().getJobName());
                for (Throwable throwable : jobExecution.getAllFailureExceptions()) {
                    log.error("\n\t{}", throwable.getMessage(), throwable);
                }
            }
        }
    }
}