package edu.charlotte.parser.jobs.generation;

import edu.charlotte.parser.listeners.common.JobLoggingListener;
import edu.charlotte.parser.ast.generation.RelDlAstGenerationProcess;
import edu.charlotte.parser.grammars.GenerateAstForRelDl;
import edu.charlotte.parser.utils.Constants;
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
public class RelDlAstGenerationJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public RelDlAstGenerationJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("Initialized RelDlAstGenerationJobConfig with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public GenerateAstForRelDl generateAstForRelDl(@Value("#{jobParameters['" + Constants.JOB_NAME + "']}") String jobName) {
        boolean hasKeYMaeraXConversion = jobName.equalsIgnoreCase(Constants.JOBNAME_REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION);
        log.debug("The job '{}' requires KeYMaeraX conversion: {}", jobName, hasKeYMaeraXConversion);
        log.info("GenerateASTForRelDL will be instantiated with hasKeYMaeraXConversion set to: {}", hasKeYMaeraXConversion);
        return new GenerateAstForRelDl(hasKeYMaeraXConversion);
    }

    @Bean
    @StepScope
    public RelDlAstGenerationProcess relDlAstGenerationProcess(GenerateAstForRelDl generateAstForRelDl) {
        log.debug("Creating step-scoped RelDlAstGenerationProcess bean.");
        return new RelDlAstGenerationProcess(generateAstForRelDl);
    }

    @Bean
    public Step relDlAstGenerationStep(ItemReader<String> inputFileReader,
                                       RelDlAstGenerationProcess relDlAstGenerationProcess,
                                       FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring relDlAstGenerationStep with chunk size: {}.", this.chunkSize);
        return new StepBuilder("relDlAstGenerationStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(relDlAstGenerationProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadRelDlAstGenerationJob(JobRepository jobRepository,
                                         JobLoggingListener jobLoggingListener,
                                         Step relDlAstGenerationStep) {
        log.info("Configuring loadRelDlAstGenerationJob.");
        return new JobBuilder("loadRelDlAstGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(relDlAstGenerationStep)
                .build();
    }
}