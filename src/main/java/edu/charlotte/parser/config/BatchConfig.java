package edu.charlotte.parser.config;

import edu.charlotte.parser.utils.Constants;
import edu.charlotte.parser.utils.ParserUtils;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class BatchConfig implements ApplicationRunner {

    // Using final as dependencies are injected via constructor
    private final Job loadDlAstGenerationJob;
    private final Job loadRelDlAstGenerationJob;
    private final Job loadDRealAstGenerationJob;
    private final Job loadDlToKeYmaeraXConversionJob;
    private final Job loadRelDlToKeYmaeraXConversionJob;
    private final Job loadDlToDRealConversionJob;
    //    private final Job loadRelDlCombiningTwoFilesJob;
    private final Job loadDlCombiningTwoFilesJob;
    private final JobLauncher jobLauncher;
    private final String outputFilePath;

    // Constructor injection for all dependencies
    public BatchConfig(
            Job loadDlAstGenerationJob,
            Job loadRelDlAstGenerationJob,
            Job loadDRealAstGenerationJob,
            Job loadDlToKeYmaeraXConversionJob,
            Job loadRelDlToKeYmaeraXConversionJob,
            Job loadDlToDRealConversionJob,
//            Job loadRelDlCombiningTwoFilesJob,
            Job loadDlCombiningTwoFilesJob,
            JobLauncher jobLauncher,
            @Value("${dl-output}") String outputFilePath) {
        this.loadDlAstGenerationJob = loadDlAstGenerationJob;
        this.loadRelDlAstGenerationJob = loadRelDlAstGenerationJob;
        this.loadDRealAstGenerationJob = loadDRealAstGenerationJob;
        this.loadDlToKeYmaeraXConversionJob = loadDlToKeYmaeraXConversionJob;
        this.loadRelDlToKeYmaeraXConversionJob = loadRelDlToKeYmaeraXConversionJob;
        this.loadDlToDRealConversionJob = loadDlToDRealConversionJob;
//        this.loadRelDlCombiningTwoFilesJob = loadRelDlCombiningTwoFilesJob;
        this.loadDlCombiningTwoFilesJob = loadDlCombiningTwoFilesJob;

        this.jobLauncher = jobLauncher;
        this.outputFilePath = outputFilePath;
        log.debug("Batch Config is initialized.");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long start = System.currentTimeMillis();

        // To ensure job option is present
        containsArgument(args.containsOption(Constants.JOB_NAME), Constants.ERROR_MESSAGE_FOR_MISSING_JOB_PARAMETERS);
        String jobName = args.getOptionValues(Constants.JOB_NAME).getFirst();
        checkJobArgumentValidity(jobName);
        try {
            JobType type = JobType.getJobType(jobName);

            if (jobName.equals(Constants.JOBNAME_REL_DL_TWO_FILES_COMBINING) || jobName.equals(Constants.JOBNAME_DL_TWO_FILES_COMBINING)) {
                containsArgument((args.containsOption(Constants.PRE_AND_POST_CONDITION_INPUT_FILE) && args.containsOption(Constants.INPUT_FILE1) && args.containsOption(Constants.INPUT_FILE2) &&
                        args.containsOption(Constants.CONSTANT_VALUE)), Constants.ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETERS);
                String conditionsInputFile = args.getOptionValues(Constants.PRE_AND_POST_CONDITION_INPUT_FILE).getFirst();
                String inputFile1 = args.getOptionValues(Constants.INPUT_FILE1).getFirst();
                String inputFile2 = args.getOptionValues(Constants.INPUT_FILE2).getFirst();
                int constantValue = Integer.parseInt(args.getOptionValues(Constants.CONSTANT_VALUE).getFirst());
                ParserUtils.validateInputFilePathIsNotNull(conditionsInputFile);
                ParserUtils.validateInputFilePathIsNotNull(inputFile1);
                ParserUtils.validateInputFilePathIsNotNull(inputFile2);
                log.info("Job Name to be parsed: {}, Input Files to be parsed are: {}, {}, {}, Constant value is: {}", jobName, conditionsInputFile,
                        inputFile1, inputFile2, constantValue);
                JobParameters jobParameters = createJobParams(jobName, conditionsInputFile, inputFile1, inputFile2, constantValue, type.getFileExtension());
                executeJob(type, jobParameters);
            } else {
                containsArgument(args.containsOption(Constants.INPUT_FILE), Constants.ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETER);
                String inputFile = args.getOptionValues(Constants.INPUT_FILE).getFirst();
                ParserUtils.validateInputFilePathIsNotNull(inputFile);
                log.info("Job Name to be parsed: {}, Input File to be parsed: {}", jobName, inputFile);
                JobParameters jobParameters = createJobParams(jobName, inputFile, type.getFileExtension());
                executeJob(type, jobParameters);
            }

            if (log.isInfoEnabled()) {
                log.info("Job execution took {} ms", System.currentTimeMillis() - start);
            }
        } catch (IllegalArgumentException e) {
            // To catch the invalid job name identified by the enum's getJobType method.
            log.error("Invalid Job: {}", jobName.toUpperCase(), e);
            throw new JobParametersInvalidException("Invalid job name specified: " + jobName);
        }
    }

    private void containsArgument(boolean isOptionPresent, String errorMessage) throws JobParametersInvalidException {
        if (!isOptionPresent) {
            log.error(errorMessage);
            throw new JobParametersInvalidException(errorMessage);
        }
    }

    private void checkJobArgumentValidity(String jobName) {
        if (jobName == null || jobName.trim().isEmpty()) {
            log.error("Job name ({}) cannot be null or empty.", jobName);
            throw new IllegalArgumentException("Job name cannot be null or empty.");
        }
    }

    private JobParameters createJobParams(String jobName, String inputFile, String fileExtension) {
        File input = ParserUtils.checkingInputFileValidity(inputFile);
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

    private JobParameters createJobParams(String jobName, String conditionsInputFile, String inputFile1, String inputFile2, int constantValue, String fileExtension) {
        File input1 = ParserUtils.checkingInputFileValidity(inputFile1);
        File input2 = ParserUtils.checkingInputFileValidity(inputFile2);

        // Use Paths.get for robust path handling and joining.
        String outputFileName = fileExtension + input1.getName() + "_" + input2.getName();
        String outputPath = Paths.get(this.outputFilePath, outputFileName).toString();
        log.info("Output file set to: {}", outputPath);

        // Add a unique run.id parameter to ensure job parameters are always unique which helps in preventing JobInstanceAlreadyCompleteException on subsequent runs with same file.
        JobParameters params = new JobParametersBuilder()
                .addString(Constants.JOB_NAME, jobName)
                .addString(Constants.INPUT_FILE1, inputFile1)
                .addString(Constants.PRE_AND_POST_CONDITION_INPUT_FILE, conditionsInputFile)
                .addString(Constants.INPUT_FILE2, inputFile2)
                .addLong(Constants.CONSTANT_VALUE, (long) constantValue)
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
    private void executeJob(JobType type, JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        switch (type) {
            case DL_AST_GENERATION -> jobLauncher.run(loadDlAstGenerationJob, jobParameters);
            case DL_TO_KEYMAERAX_OUTPUT_CONVERSION -> jobLauncher.run(loadDlToKeYmaeraXConversionJob, jobParameters);
            case REL_DL_AST_GENERATION -> jobLauncher.run(loadRelDlAstGenerationJob, jobParameters);
            case REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION ->
                    jobLauncher.run(loadRelDlToKeYmaeraXConversionJob, jobParameters);
            case D_REAL_AST_GENERATION -> jobLauncher.run(loadDRealAstGenerationJob, jobParameters);
            case REL_DL_TO_D_REAL_OUTPUT_CONVERSION -> jobLauncher.run(loadDlToDRealConversionJob, jobParameters);
//            case REL_DL_TWO_FILES_COMBINING -> jobLauncher.run(loadRelDlCombiningTwoFilesJob, jobParameters);
            case DL_TWO_FILES_COMBINING -> jobLauncher.run(loadDlCombiningTwoFilesJob, jobParameters);
        }
    }
}