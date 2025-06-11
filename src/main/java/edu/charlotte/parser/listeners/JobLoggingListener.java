package edu.charlotte.parser.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class JobLoggingListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job {} started at: {}", jobExecution.getJobInstance().getJobName(), jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job {} completed with status: {} at {}", jobExecution.getJobInstance().getJobName(), jobExecution.getStatus(), jobExecution.getEndTime());
    }
}
