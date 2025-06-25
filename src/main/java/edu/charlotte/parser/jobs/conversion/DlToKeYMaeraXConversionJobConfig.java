package edu.charlotte.parser.jobs.conversion;

import edu.charlotte.parser.listeners.common.JobLoggingListener;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYMaeraXConverter;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeyMaeraXConversionProcess;
import edu.charlotte.parser.conversions.common.GenerateKeYMaeraXOutput;
import edu.charlotte.parser.grammars.GenerateAstForDl;
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
public class DlToKeYMaeraXConversionJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DlToKeYMaeraXConversionJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("DlToKeYMaeraXConversionJobConfig is initialized with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public DlToKeyMaeraXConversionProcess dlToKeyMaeraXConversionProcess(GenerateAstForDl generateAstForDl, GenerateKeYMaeraXOutput generateKeYMaeraXOutput,
                                                                         DlToKeYMaeraXConverter dlToKeYMaeraXConverter) {
        log.debug("Creating step-scoped DlToKeyMaeraXConversionProcess bean.");
        return new DlToKeyMaeraXConversionProcess(generateAstForDl, generateKeYMaeraXOutput, dlToKeYMaeraXConverter);
    }

    @Bean
    public Step dlToKeYMaeraXConversionStep(ItemReader<String> inputFileReader,
                                            DlToKeyMaeraXConversionProcess dlToKeyMaeraXConversionProcess,
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
    public Job loadDlToKeYMaeraXConversionJob(JobRepository jobRepository,
                                              JobLoggingListener jobLoggingListener,
                                              Step dlToKeYMaeraXConversionStep) {
        log.debug("Configuring loadDlToKeYMaeraXConversionJob.");
        return new JobBuilder("loadDlToKeYMaeraXConversionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dlToKeYMaeraXConversionStep)
                .build();
    }
}