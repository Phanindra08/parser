package edu.charlotte.parser.jobs;

import edu.charlotte.parser.listeners.JobLoggingListener;
import edu.charlotte.parser.parser_conversion.RelDLAstGenerationProcess;
import edu.charlotte.parser.parser_for_grammars.GenerateASTForRelDL;
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
public class RelDLASTGenerationJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public RelDLASTGenerationJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("Initialized RelDLASTGenerationJobConfig with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public GenerateASTForRelDL generateASTForRelDL() {
        log.debug("Creating step-scoped GenerateASTForRelDL bean.");
        return new GenerateASTForRelDL();
    }

    @Bean
    @StepScope
    public RelDLAstGenerationProcess relDLAstGenerationProcess(GenerateASTForRelDL generateASTForRelDL) {
        log.debug("Creating step-scoped RelDLAstGenerationProcess bean.");
        return new RelDLAstGenerationProcess(generateASTForRelDL);
    }

    @Bean
    public Step relDlAstGenerationStep(ItemReader<String> inputFileReader,
                                       RelDLAstGenerationProcess relDLAstGenerationProcess,
                                       FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring relDlAstGenerationStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("relDlAstGenerationStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(relDLAstGenerationProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadRelDLASTGenerationJob(JobRepository jobRepository,
                                         JobLoggingListener jobLoggingListener,
                                         Step relDlAstGenerationStep) {
        log.info("Configuring loadRelDLASTGenerationJob.");
        return new JobBuilder("loadRelDLASTGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(relDlAstGenerationStep)
                .build();
    }
}