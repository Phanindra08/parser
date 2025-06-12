package edu.charlotte.parser.jobs;

import edu.charlotte.parser.listeners.JobLoggingListener;
import edu.charlotte.parser.parser_conversion.DLToKeyMaeraXConversionProcess;
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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class DLToKeYMaeraXConversionJobConfig {

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    JobRepository jobRepository;

    @Bean
    @StepScope
    public DLToKeyMaeraXConversionProcess getDLToKeyMaeraXConversionProcessObject() {
        return new DLToKeyMaeraXConversionProcess();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> keYMaeraXOutputFile(@Value("#{jobParameters['" + Constants.OUTPUT_FILE + "']}") String outputFile) {
        if (outputFile == null || outputFile.trim().isEmpty()) {
            log.error("Output file parameter {} is null or empty", Constants.OUTPUT_FILE);
            throw new IllegalArgumentException("Output file path cannot be null or empty");
        }
        log.info("Writing to the keYMaeraXOutputFile {}.", outputFile);
        // Configure FlatFileItemWriter
        return new FlatFileItemWriterBuilder<String>()
                .name("keYMaeraXOutputFile")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

    @Bean
    public Step dlToKeYMaeraXConversionStep(ItemReader<String> dlFileReader, FlatFileItemWriter<String> keYMaeraXOutputFile) {
        return new StepBuilder("dlToKeYMaeraXConversionStep", jobRepository)
                .<String, String>chunk(50, transactionManager)
                .reader(dlFileReader)
                .processor(getDLToKeyMaeraXConversionProcessObject())
                .writer(keYMaeraXOutputFile)
                .build();
    }

    @Bean
    public Job loadDLToKeYMaeraXConversionJob(JobRepository jobRepository, Step dlToKeYMaeraXConversionStep) {
        return new JobBuilder("loadDLToKeYMaeraXConversionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobLoggingListener())
                .start(dlToKeYMaeraXConversionStep)
                .build();
    }
}
