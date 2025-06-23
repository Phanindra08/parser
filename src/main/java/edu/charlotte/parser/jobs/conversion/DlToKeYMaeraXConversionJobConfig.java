package edu.charlotte.parser.jobs.conversion_jobs;

import edu.charlotte.parser.listeners.JobLoggingListener;
import edu.charlotte.parser.parser_conversions.dl_to_keymaerax_conversion.DLToKeYMaeraXConverter;
import edu.charlotte.parser.parser_conversions.dl_to_keymaerax_conversion.DLToKeyMaeraXConversionProcess;
import edu.charlotte.parser.parser_conversions.GenerateKeYMaeraXOutput;
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
public class DLToKeYMaeraXConversionJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DLToKeYMaeraXConversionJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("DLToKeYMaeraXConversionJobConfig is initialized with chunk size: {}", this.chunkSize);
    }

    @Bean
    public GenerateKeYMaeraXOutput generateKeYMaeraXOutput() {
        log.debug("Creating GenerateKeYMaeraXOutput bean.");
        return new GenerateKeYMaeraXOutput();
    }

    @Bean
    public DLToKeYMaeraXConverter dlToKeYMaeraXConverter() {
        log.debug("Creating DLToKeYMaeraXConverter bean.");
        return new DLToKeYMaeraXConverter();
    }

    @Bean
    @StepScope
    public DLToKeyMaeraXConversionProcess dlToKeyMaeraXConversionProcess(GenerateASTForDL generateASTForDL, GenerateKeYMaeraXOutput generateKeYMaeraXOutput,
                                                                         DLToKeYMaeraXConverter dlToKeYMaeraXConverter) {
        log.debug("Creating step-scoped DLToKeyMaeraXConversionProcess bean.");
        return new DLToKeyMaeraXConversionProcess(generateASTForDL, generateKeYMaeraXOutput, dlToKeYMaeraXConverter);
    }

    @Bean
    public Step dlToKeYMaeraXConversionStep(ItemReader<String> inputFileReader,
                                            DLToKeyMaeraXConversionProcess dlToKeyMaeraXConversionProcess,
                                            FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring dlToKeYMaeraXConversionStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("dlToKeYMaeraXConversionStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(dlToKeyMaeraXConversionProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadDLToKeYMaeraXConversionJob(JobRepository jobRepository,
                                              JobLoggingListener jobLoggingListener,
                                              Step dlToKeYMaeraXConversionStep) {
        log.debug("Configuring loadDLToKeYMaeraXConversionJob.");
        return new JobBuilder("loadDLToKeYMaeraXConversionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dlToKeYMaeraXConversionStep)
                .build();
    }
}