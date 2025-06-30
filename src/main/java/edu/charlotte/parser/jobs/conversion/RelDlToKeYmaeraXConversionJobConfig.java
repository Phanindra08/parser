package edu.charlotte.parser.jobs.conversion;

import edu.charlotte.parser.listeners.common.JobLoggingListener;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYmaeraXConverter;
import edu.charlotte.parser.conversions.common.GenerateKeYmaeraXOutput;
import edu.charlotte.parser.conversions.reldl.keymaerax.RelDlToKeYmaeraXConversionProcess;
import edu.charlotte.parser.grammars.GenerateAstForRelDl;
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
public class RelDlToKeYmaeraXConversionJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public RelDlToKeYmaeraXConversionJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("RelDlToKeYmaeraXConversionJobConfig is initialized with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public RelDlToKeYmaeraXConversionProcess relDlToKeYmaeraXConversionProcess(GenerateAstForRelDl generateAstForRelDl, GenerateKeYmaeraXOutput generateKeYmaeraXOutput,
                                                                               DlToKeYmaeraXConverter dlToKeYmaeraXConverter) {
        log.debug("Creating step-scoped RelDlToKeYmaeraXConversionProcess bean.");
        return new RelDlToKeYmaeraXConversionProcess(generateAstForRelDl, generateKeYmaeraXOutput, dlToKeYmaeraXConverter);
    }

    @Bean
    public Step relDlToKeYmaeraXConversionStep(ItemReader<String> inputFileReader,
                                               RelDlToKeYmaeraXConversionProcess relDlToKeYmaeraXConversionProcess,
                                               FlatFileItemWriter<String> outputFileWriter) {
        log.info("Configuring relDlToKeYmaeraXConversionStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("relDlToKeYmaeraXConversionStep", jobRepository)
                .<String, String>chunk(chunkSize, transactionManager)
                .reader(inputFileReader)
                .processor(relDlToKeYmaeraXConversionProcess)
                .writer(outputFileWriter)
                .build();
    }

    @Bean
    public Job loadRelDlToKeYmaeraXConversionJob(JobRepository jobRepository,
                                                 JobLoggingListener jobLoggingListener,
                                                 Step relDlToKeYmaeraXConversionStep) {
        log.debug("Configuring loadRelDlToKeYmaeraXConversionJob.");
        return new JobBuilder("loadRelDlToKeYmaeraXConversionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(relDlToKeYmaeraXConversionStep)
                .build();
    }
}