package edu.charlotte.parser.jobs.ast_generation_config;

import edu.charlotte.parser.listeners.JobLoggingListener;
import edu.charlotte.parser.ast.generation.DlAstGenerationProcess;
import edu.charlotte.parser.parser_for_grammars.GenerateASTForDL;
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
public class DLASTGenerationJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DLASTGenerationJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("Initialized DLASTGenerationJobConfig with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public GenerateASTForDL generateASTForDL() {
        log.debug("Creating step-scoped GenerateASTForDL bean.");
        return new GenerateASTForDL();
    }

    @Bean
    @StepScope
    public DlAstGenerationProcess dlAstGenerationProcessor(GenerateASTForDL generateASTForDL) {
        log.debug("Creating step-scoped DlAstGenerationProcess bean.");
        return new DlAstGenerationProcess(generateASTForDL);
    }

    @Bean
    public Step dlAstGenerationStep(ItemReader<String> inputFileReader,
                                    DlAstGenerationProcess dlAstGenerationProcessor,
                                    FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring dlAstGenerationStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("dlAstGenerationStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(dlAstGenerationProcessor)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public JobLoggingListener jobLoggingListener() {
        return new JobLoggingListener();
    }

    @Bean
    public Job loadDlAstGenerationJob(JobRepository jobRepository,
                                      JobLoggingListener jobLoggingListener,
                                      Step dlAstGenerationStep) {
        log.debug("Configuring loadDlAstGenerationJob.");
        return new JobBuilder("loadDlAstGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dlAstGenerationStep)
                .build();
    }
}