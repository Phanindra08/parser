package edu.charlotte.parser.jobs;

import edu.charlotte.parser.listeners.JobLoggingListener;
import edu.charlotte.parser.parser_conversion.DlAstGenerationProcess;
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
public class DLASTGenerationJobConfig {
    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    JobRepository jobRepository;

    @Bean
    @StepScope
    public DlAstGenerationProcess getDlAstGenerationObject() {
        return new DlAstGenerationProcess();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> dlAstOutputFile(@Value("#{jobParameters['" + Constants.OUTPUT_FILE + "']}") String outputFile) {
        if (outputFile == null || outputFile.trim().isEmpty()) {
            log.error("Output file parameter {} is null or empty", Constants.OUTPUT_FILE);
            throw new IllegalArgumentException("Output file path cannot be null or empty");
        }
        log.info("Writing to the DL AST Output file {}.", outputFile);
        // Configure FlatFileItemWriter
        return new FlatFileItemWriterBuilder<String>()
                .name("dlAstOutputFile")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

    @Bean
    public Step dlAstGenerationStep(ItemReader<String> dlFileReader, FlatFileItemWriter<String> dlAstOutputFile) {
        return new StepBuilder("dlAstGenerationStep", jobRepository)
                .<String, String>chunk(Constants.CHUNK_SIZE, transactionManager)
                .reader(dlFileReader)
                .processor(getDlAstGenerationObject())
                .writer(dlAstOutputFile)
                .build();
    }

    @Bean
    public Job loadDlAstGenerationJob(JobRepository jobRepository, Step dlAstGenerationStep) {
        return new JobBuilder("loadDlAstGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobLoggingListener())
                .start(dlAstGenerationStep)
                .build();
    }
}
