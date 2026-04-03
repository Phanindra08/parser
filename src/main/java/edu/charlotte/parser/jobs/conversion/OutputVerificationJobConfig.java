package edu.charlotte.parser.jobs.conversion;

import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Configuration
@Slf4j
public class OutputVerificationJobConfig {

    public OutputVerificationJobConfig() {
        log.info("OutputVerificationJobConfig is initialized.");
    }

    @Bean
    public Step verifyWithKeYMaeraXStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                        Tasklet taskletToVerifyWithKeYMaeraX) {

        log.info("Configuring verifyWithKeYMaeraXStep");
        return new StepBuilder("verifyWithKeYMaeraXStep", jobRepository)
                .tasklet(taskletToVerifyWithKeYMaeraX, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet taskletToVerifyWithKeYMaeraX(@Value("#{jobParameters['" + Constants.OUTPUT_FILE + "']}") String outputFilePath) {
        return (contribution, chunkContext) -> {
            log.info("Starting KeYMaeraX Verification");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    "libs/keymaerax.jar",
                    "-prove",
                    outputFilePath
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isProvable = true;
            while ((line = reader.readLine()) != null) {
                log.debug("The line read is {}.", line);
                if (line.contains(Constants.VERIFICATION_FAILED) ||
                        line.contains(Constants.VERIFICATION_FALSE))
                    isProvable = false;
            }

            log.info("Verification result of KeYMaeraX is: {}.", isProvable);
            process.waitFor();
            return RepeatStatus.FINISHED;
        };
    }
}
