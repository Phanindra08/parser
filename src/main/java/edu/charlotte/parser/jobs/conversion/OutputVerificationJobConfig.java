package edu.charlotte.parser.jobs.conversion;

import edu.charlotte.parser.utils.Constants;
import lombok.NonNull;
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
import java.io.File;
import java.io.IOException;
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
    public Step verifyWithDRealStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                    Tasklet taskletToVerifyWithDReal) {

        log.info("Configuring verifyWithDRealStep");
        return new StepBuilder("verifyWithDRealStep", jobRepository)
                .tasklet(taskletToVerifyWithDReal, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet taskletToVerifyWithKeYMaeraX(
            @Value("#{jobParameters['" + Constants.OUTPUT_FILE + "']}") String outputFilePath,
            @Value("${keymaerax.jar.path:libs/keymaerax.jar}") String keymaeraXJarPath) {
        return (contribution, chunkContext) -> {
            log.info("Starting KeYMaeraX Verification for the file: {}", outputFilePath);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "-jar",
                    keymaeraXJarPath,
                    "-prove",
                    outputFilePath
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder fullOutput = new StringBuilder();
            Boolean isProvable = null;
            while ((line = reader.readLine()) != null) {
                log.debug("The output line read for the execution of KeYMaeraX file is {}.", line);
                fullOutput.append(line).append(System.lineSeparator());
                if (line.contains(Constants.VERIFICATION_FAILED) ||
                        line.contains(Constants.VERIFICATION_FALSE))
                    isProvable = false;
                else if (line.contains(Constants.VERIFICATION_PROVED))
                    isProvable = true;
            }

            int exitCode = process.waitFor();
            log.info("KeYMaeraX process completed with exit code: {}", exitCode);

            if (exitCode != 0)
                throw new RuntimeException("KeYMaeraX verification failed with exit code: " + exitCode
                        + ". Output: " + fullOutput);

            if (isProvable == null)
                log.warn("Unable to determine KeYMaeraX verification result from output.");
            else
                log.info("Verification result of KeYMaeraX is: {}.", isProvable);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @StepScope
    public Tasklet taskletToVerifyWithDReal(
            @Value("#{jobParameters['" + Constants.OUTPUT_FILE + "']}") String outputFilePath,
            @Value("${d-real.docker.image:dreal/dreal3}") String dRealDockerImage,
            @Value("${mounted-file-path:/data/}") String mountedFilePath) {

        return (contribution, chunkContext) -> {
            log.info("Starting dReal Verification for the file: {}", outputFilePath);

            Process process = getProcess(outputFilePath, dRealDockerImage, mountedFilePath);
            StringBuilder fullOutput = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("The output line read for the execution of the SMT file is: {}", line);
                    fullOutput.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();
            log.info("dReal process completed with exit code: {}", exitCode);
            String result = fullOutput.toString();
            if (exitCode != 0)
                throw new RuntimeException("dReal verification failed with exit code: " + exitCode
                        + ". Output: " + result);

            if (result.contains(Constants.D_REAL_UNSAT))
                log.info("dReal verification result: UNSAT");
            else if (result.contains(Constants.D_REAL_DELTA_SAT))
                log.info("dReal verification result: DELTA-SAT");
            else if (result.contains(Constants.D_REAL_SAT))
                log.info("dReal verification result: SAT");
            else
                log.warn("Unable to determine dReal verification result from output.");

            return RepeatStatus.FINISHED;
        };
    }

    private static @NonNull Process getProcess(String outputFilePath, String dRealDockerImage, String mountedFilePath) throws IOException {
        File outputFile = new File(outputFilePath);
        if (!outputFile.exists())
            throw new RuntimeException("Output file does not exist: " + outputFilePath);

        File parentDir = outputFile.getParentFile();
        if (parentDir == null || !parentDir.exists())
            throw new RuntimeException("Parent directory does not exist for the file: " + outputFilePath);

        String fileName = outputFile.getName();
//        String absolutePath = parentDir.getAbsolutePath().replace(':');

        ProcessBuilder processBuilder = new ProcessBuilder(
                Constants.DOCKER,
                Constants.RUN,
                Constants.REMOVE,
                Constants.V,
                parentDir.getAbsolutePath() + Constants.COLON + mountedFilePath,
                dRealDockerImage,
                Constants.D_REAL,
                mountedFilePath + fileName
        );
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}