package edu.charlotte.parser.jobs.generation;

import edu.charlotte.parser.ast.generation.DRealAstGenerationProcess;
import edu.charlotte.parser.grammars.GenerateAstForDReal;
import edu.charlotte.parser.listeners.common.JobLoggingListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class DRealAstGenerationJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DRealAstGenerationJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("Initialized DRealAstGenerationJobConfig with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public DRealAstGenerationProcess dRealAstGenerationProcess(GenerateAstForDReal generateAstForDReal) {
        log.debug("Creating step-scoped dRealAstGenerationProcess bean.");
        return new DRealAstGenerationProcess(generateAstForDReal);
    }

    @Bean
    public Step dRealAstGenerationStep(ItemReader<String> inputFileReader,
                                       DRealAstGenerationProcess dRealAstGenerationProcess,
                                       FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring dRealAstGenerationStep with chunk size: {}.", this.chunkSize);
        return new StepBuilder("dRealAstGenerationStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(dRealAstGenerationProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadDRealAstGenerationJob(JobRepository jobRepository,
                                         JobLoggingListener jobLoggingListener,
                                         Step dRealAstGenerationStep) {
        log.info("Configuring loadDRealAstGenerationJob.");
        return new JobBuilder("loadDRealAstGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dRealAstGenerationStep)
                .build();
    }
}