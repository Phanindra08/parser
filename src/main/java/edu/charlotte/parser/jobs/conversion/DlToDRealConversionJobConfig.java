package edu.charlotte.parser.jobs.conversion;

import edu.charlotte.parser.conversions.common.GenerateDRealOutput;
import edu.charlotte.parser.conversions.dl.dreal.DlToDRealConversionProcess;
import edu.charlotte.parser.conversions.dl.dreal.DlToDRealConverter;
import edu.charlotte.parser.grammars.GenerateAstForDl;
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
public class DlToDRealConversionJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DlToDRealConversionJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("DlToDRealConversionJobConfig is initialized with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public DlToDRealConversionProcess dlToDRealConversionProcess(GenerateDRealOutput generateDRealOutput, DlToDRealConverter dlToDRealConverter) {
        log.debug("Creating step-scoped dlToDRealConversionProcess bean.");
        return new DlToDRealConversionProcess(new GenerateAstForDl(), generateDRealOutput, dlToDRealConverter);
    }

    @Bean
    public Step dlToDRealConversionStep(ItemReader<String> inputFileReader,
                                        DlToDRealConversionProcess dlToDRealConversionProcess,
                                        FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring dlToDRealConversionStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("dlToDRealConversionStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(dlToDRealConversionProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadDlToDRealConversionJob(JobRepository jobRepository,
                                          JobLoggingListener jobLoggingListener,
                                          Step dlToDRealConversionStep) {
        log.debug("Configuring loadDlToDRealConversionJob.");
        return new JobBuilder("loadDlToDRealConversionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dlToDRealConversionStep)
                .build();
    }
}