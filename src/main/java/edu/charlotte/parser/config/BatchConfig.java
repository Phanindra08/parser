package edu.charlotte.parser.config;

import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class BatchConfig implements ApplicationRunner {

    // Using final as dependencies are injected via constructor
    private final Job loadDlAstGenerationJob;
    private final Job loadRelDlAstGenerationJob;
    private final Job loadDlToKeYmaeraXConversionJob;
    private final Job loadRelDlToKeYmaeraXConversionJob;
    private final JobLauncher jobLauncher;
    private final String outputFilePath;

    // Constructor injection for all dependencies
    public BatchConfig(
            Job loadDlAstGenerationJob,
            Job loadRelDlAstGenerationJob,
            Job loadDlToKeYmaeraXConversionJob,
            Job loadRelDlToKeYmaeraXConversionJob,
            JobLauncher jobLauncher,
            @Value("${dl-output}") String outputFilePath) {
        this.loadDlAstGenerationJob = loadDlAstGenerationJob;
        this.loadRelDlAstGenerationJob = loadRelDlAstGenerationJob;
        this.loadDlToKeYmaeraXConversionJob = loadDlToKeYmaeraXConversionJob;
        this.loadRelDlToKeYmaeraXConversionJob = loadRelDlToKeYmaeraXConversionJob;
        this.jobLauncher = jobLauncher;
        this.outputFilePath = outputFilePath;
        log.debug("Batch Config is initialized.");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long start = System.currentTimeMillis();

        // To ensure required options are present
        if (!args.containsOption("job.name") || !args.containsOption("input.file")) {
            log.error(Constants.ERROR_MESSAGE_FOR_MISSING_JOB_PARAMETERS);
            throw new JobParametersInvalidException(Constants.ERROR_MESSAGE_FOR_MISSING_JOB_PARAMETERS);
        }

        String jobName = args.getOptionValues("job.name").getFirst();
        String inputFile = args.getOptionValues("input.file").getFirst();
        runJob(jobName, inputFile);
        if (log.isInfoEnabled()) {
            log.info("Job execution took {} ms", System.currentTimeMillis() - start);
        }
    }

    private void runJob(String jobName, String inputFile) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        checkArgumentsValidity(jobName, inputFile);
        executeJob(jobName, inputFile);
    }

    private void checkArgumentsValidity(String jobName, String inputFile) {
        if (jobName == null || jobName.trim().isEmpty()) {
            log.error("Job name ({}) cannot be null or empty.", jobName);
            throw new IllegalArgumentException("Job name cannot be null or empty.");
        }
        if (inputFile == null || inputFile.trim().isEmpty()) {
            log.error("Input file path ({}) cannot be null or empty.", inputFile);
            throw new IllegalArgumentException("Input file path cannot be null or empty.");
        }
        log.info("Job Name to be parsed: {}, Input File to be parsed: {}", jobName, inputFile);
    }

    private JobParameters createJobParams(String jobName, String inputFile, String fileExtension) {
        File input = new File(inputFile);
        if (!input.exists() || !input.isFile()) {
            log.error("Input file ({}) does not exist or is not a file.", inputFile);
            throw new IllegalArgumentException("Input file does not exist or is not a file: " + inputFile);
        }

        // Use Paths.get for robust path handling and joining.
        String outputFileName = input.getName() + fileExtension;
        String outputPath = Paths.get(this.outputFilePath, outputFileName).toString();
        log.info("Output file set to: {}", outputPath);

        // Add a unique run.id parameter to ensure job parameters are always unique which helps in preventing JobInstanceAlreadyCompleteException on subsequent runs with same file.
        JobParameters params = new JobParametersBuilder()
                .addString(Constants.JOB_NAME, jobName)
                .addString(Constants.INPUT_FILE, inputFile)
                .addString(Constants.OUTPUT_FILE, outputPath)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        log.debug("Job Parameters created: {}", params);
        return params;
    }

    /**
     * We will use the JobType enum to get the correct job identifier and file extension.
     * We will select the job based on the enum type.
     */
    private void executeJob(String jobName, String inputFile)
            throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {

        JobParameters jobParameters;
        try {
            JobType type = JobType.getJobType(jobName);
            jobParameters = createJobParams(jobName, inputFile, type.getFileExtension());

            switch (type) {
                case DL_AST_GENERATION -> jobLauncher.run(loadDlAstGenerationJob, jobParameters);
                case DL_TO_KEYMAERAX_OUTPUT_CONVERSION -> jobLauncher.run(loadDlToKeYmaeraXConversionJob, jobParameters);
                case REL_DL_AST_GENERATION -> jobLauncher.run(loadRelDlAstGenerationJob, jobParameters);
                case REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION -> jobLauncher.run(loadRelDlToKeYmaeraXConversionJob, jobParameters);
            }
        } catch (IllegalArgumentException e) {
            // To catch the invalid job name identified by the enum's getJobType method.
            log.error("Invalid Job: {}", jobName.toUpperCase(), e);
            throw new JobParametersInvalidException("Invalid job name specified: " + jobName);
        }
    }
}