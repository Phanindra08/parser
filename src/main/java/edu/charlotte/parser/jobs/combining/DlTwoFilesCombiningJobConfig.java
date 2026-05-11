package edu.charlotte.parser.jobs.combining;

import edu.charlotte.parser.combining.GenerateCombinedOutput;
import edu.charlotte.parser.combining.dl.DlTwoFileCombining;
import edu.charlotte.parser.combining.dl.DlTwoFilesCombiningProcess;
import edu.charlotte.parser.conversions.dl.keymaerax.DlToKeYmaeraXConverter;
import edu.charlotte.parser.dto.MultipleFileContentDTO;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import edu.charlotte.parser.listeners.common.ExecutionStepListener;
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
public class DlTwoFilesCombiningJobConfig {
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final int chunkSize;

    public DlTwoFilesCombiningJobConfig(
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository,
            @Value("${chunk-size:10}") int chunkSize) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.chunkSize = chunkSize;
        log.info("DlTwoFilesCombiningJobConfig is initialized with chunk size: {}", this.chunkSize);
    }

    @Bean
    @StepScope
    public DlTwoFilesCombiningProcess dlTwoFilesCombiningProcess(GenerateCombinedOutput generateCombinedDlOutput, DlTwoFileCombining dlTwoFileCombining,
                                                                 DlToKeYmaeraXConverter dlToKeYmaeraXConverter) {
        log.debug("Creating step-scoped dlTwoFilesCombiningProcess bean.");
        return new DlTwoFilesCombiningProcess(new GenerateAstForDl(), new GenerateAstForDl(), generateCombinedDlOutput, dlTwoFileCombining, dlToKeYmaeraXConverter);
    }

    @Bean
    public Step dlCombiningTwoFilesStep(ItemReader<MultipleFileContentDTO> multipleFileReader,
                                        DlTwoFilesCombiningProcess dlTwoFilesCombiningProcess,
                                        FlatFileItemWriter<String> outputFileWriter,
                                        ExecutionStepListener executionStepListener) {
        log.info("Configuring dlCombiningTwoFilesStep with chunk size: {}", this.chunkSize);
        return new StepBuilder("dlCombiningTwoFilesStep", jobRepository)
                .<MultipleFileContentDTO, String>chunk(chunkSize, transactionManager)
                .reader(multipleFileReader)
                .processor(dlTwoFilesCombiningProcess)
                .writer(outputFileWriter)
                .listener(executionStepListener)
                .build();
    }

    @Bean
    public Job loadDlCombiningTwoFilesJob(JobRepository jobRepository,
                                          JobLoggingListener jobLoggingListener,
                                          Step dlCombiningTwoFilesStep) {
        log.debug("Configuring loadDlCombiningTwoFilesJob.");
        return new JobBuilder("loadDlCombiningTwoFilesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(dlCombiningTwoFilesStep)
//                .next(verifyWithKeYMaeraXStep)
                .build();
    }
}
