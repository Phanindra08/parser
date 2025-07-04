package edu.charlotte.parser.jobs.conversion;

import edu.charlotte.parser.listeners.common.JobLoggingListener;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYmaeraXConverter;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYmaeraXConversionProcess;
import edu.charlotte.parser.conversions.common.GenerateKeYmaeraXOutput;
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
public class DlToKeYmaeraXConversionJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DlToKeYmaeraXConversionJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("DlToKeYmaeraXConversionJobConfig is initialized with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public DlToKeYmaeraXConversionProcess dlToKeYmaeraXConversionProcess(GenerateAstForDl generateAstForDl, GenerateKeYmaeraXOutput generateKeYmaeraXOutput,
                                                                         DlToKeYmaeraXConverter dlToKeYmaeraXConverter) {
        log.debug("Creating step-scoped DlToKeYmaeraXConversionProcess bean.");
        return new DlToKeYmaeraXConversionProcess(generateAstForDl, generateKeYmaeraXOutput, dlToKeYmaeraXConverter);
    }

    @Bean
    public Step dlToKeYmaeraXConversionStep(ItemReader<String> inputFileReader,
                                            DlToKeYmaeraXConversionProcess dlToKeYmaeraXConversionProcess,
                                            FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring dlToKeYmaeraXConversionStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("dlToKeYmaeraXConversionStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(dlToKeYmaeraXConversionProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadDlToKeYmaeraXConversionJob(JobRepository jobRepository,
                                              JobLoggingListener jobLoggingListener,
                                              Step dlToKeYmaeraXConversionStep) {
        log.debug("Configuring loadDlToKeYmaeraXConversionJob.");
        return new JobBuilder("loadDlToKeYmaeraXConversionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dlToKeYmaeraXConversionStep)
                .build();
    }
}