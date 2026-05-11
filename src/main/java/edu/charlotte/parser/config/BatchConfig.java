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
    private final Job loadDlToDRealIndividualInputsConversionJob;
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
            Job loadDlCombiningTwoFilesJob,
            Job loadDlToDRealIndividualInputsConversionJob,
            JobLauncher jobLauncher,
            @Value("${dl-output}") String outputFilePath) {
        this.loadDlAstGenerationJob = loadDlAstGenerationJob;
        this.loadRelDlAstGenerationJob = loadRelDlAstGenerationJob;
        this.loadDRealAstGenerationJob = loadDRealAstGenerationJob;
        this.loadDlToKeYmaeraXConversionJob = loadDlToKeYmaeraXConversionJob;
        this.loadRelDlToKeYmaeraXConversionJob = loadRelDlToKeYmaeraXConversionJob;
        this.loadDlToDRealConversionJob = loadDlToDRealConversionJob;
        this.loadDlCombiningTwoFilesJob = loadDlCombiningTwoFilesJob;
        this.loadDlToDRealIndividualInputsConversionJob = loadDlToDRealIndividualInputsConversionJob;

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
                containsArgument((args.containsOption(Constants.INPUT_FILE1) && args.containsOption(Constants.INPUT_FILE2) &&
                        args.containsOption(Constants.CONSTANT_VALUE_FILE)), Constants.ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETERS);
                String inputFile1 = args.getOptionValues(Constants.INPUT_FILE1).getFirst();
                String inputFile2 = args.getOptionValues(Constants.INPUT_FILE2).getFirst();
                String constantValueFile = args.getOptionValues(Constants.CONSTANT_VALUE_FILE).getFirst();
                ParserUtils.validateInputFilePathIsNotNull(inputFile1);
                ParserUtils.validateInputFilePathIsNotNull(inputFile2);
                ParserUtils.validateInputFilePathIsNotNull(constantValueFile);
                log.info("Job Name to be parsed: {}, Input Files to be parsed are: {}, {}, {}", jobName, inputFile1, inputFile2, constantValueFile);
                JobParameters jobParameters = createJobParams(jobName, inputFile1, inputFile2, constantValueFile, type.getFileExtension());
                executeJob(type, jobParameters);
            } else if (jobName.equals(Constants.JOBNAME_DL_TO_D_REAL_OUTPUT_CONVERSION)) {
                containsArgument(args.containsOption(Constants.INPUT_FILE),
                        Constants.ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETER_FOR_DL_TO_DREAL_CONVERSION);
                String inputFile = args.getOptionValues(Constants.INPUT_FILE).getFirst();
                ParserUtils.validateInputFilePathIsNotNull(inputFile);
                String integrationUpperLimitFile = null;
                if (args.containsOption(Constants.INTEGRATION_UPPER_LIMIT_FILE)) {
                    integrationUpperLimitFile = args.getOptionValues(Constants.INTEGRATION_UPPER_LIMIT_FILE).getFirst();
                    ParserUtils.validateInputFilePathIsNotNull(integrationUpperLimitFile);
                }
                log.info("Job Name to be parsed: {}, Input Files to be parsed are: {}, {}", jobName, inputFile, integrationUpperLimitFile);
                JobParameters jobParameters = createJobParams(jobName, inputFile, integrationUpperLimitFile, type.getFileExtension());
                executeJob(type, jobParameters);
            } else if (jobName.equals(Constants.JOBNAME_DL_TO_D_REAL_OUTPUT_FOR_INDIVIDUAL_INPUTS)) {
                containsArgument((args.containsOption(Constants.PRE_POST_CONDITION_FILE)
                                && args.containsOption(Constants.DL_PROGRAM_FILE)
                                && args.containsOption(Constants.INTEGRATION_UPPER_LIMIT_FILE)),
                        Constants.ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETERS_FOR_INDIVIDUAL_INPUTS);

                String conditionFile = args.getOptionValues(Constants.PRE_POST_CONDITION_FILE).getFirst();
                String programFile = args.getOptionValues(Constants.DL_PROGRAM_FILE).getFirst();
                String integrationUpperLimitFile = args.getOptionValues(Constants.INTEGRATION_UPPER_LIMIT_FILE).getFirst();
                ParserUtils.validateInputFilePathIsNotNull(conditionFile);
                ParserUtils.validateInputFilePathIsNotNull(programFile);
                ParserUtils.validateInputFilePathIsNotNull(integrationUpperLimitFile);

                log.info("Job Name to be parsed: {}, Input Files to be parsed are: {}, {}, {}", jobName, conditionFile, programFile, integrationUpperLimitFile);
                JobParameters jobParameters = createJobParamsForIndividualInputs(jobName, conditionFile, programFile, integrationUpperLimitFile, type.getFileExtension());
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

    private JobParameters createJobParams(String jobName, String inputFile1, String inputFile2, String constantValueFile, String fileExtension) {
        File input1 = ParserUtils.checkingInputFileValidity(inputFile1);
        File input2 = ParserUtils.checkingInputFileValidity(inputFile2);
        ParserUtils.checkingInputFileValidity(constantValueFile);

        // Use Paths.get for robust path handling and joining.
        String outputFileName = input1.getName() + "_" + input2.getName() + fileExtension;
        String outputPath = Paths.get(this.outputFilePath, outputFileName).toString();
        log.info("Output file set to: {}", outputPath);

        // Add a unique run.id parameter to ensure job parameters are always unique which helps in preventing JobInstanceAlreadyCompleteException on subsequent runs with same file.
        JobParameters params = new JobParametersBuilder()
                .addString(Constants.JOB_NAME, jobName)
                .addString(Constants.INPUT_FILE1, inputFile1)
                .addString(Constants.INPUT_FILE2, inputFile2)
                .addString(Constants.CONSTANT_VALUE_FILE, constantValueFile)
                .addString(Constants.OUTPUT_FILE, outputPath)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        log.debug("Job Parameters created: {}", params);
        return params;
    }

    private JobParameters createJobParams(String jobName, String inputFile, String integrationUpperLimitFile, String fileExtension) {
        File inputFileAfterValidation = ParserUtils.checkingInputFileValidity(inputFile);

        // Use Paths.get for robust path handling and joining.
        String outputFileName = inputFileAfterValidation.getName() + fileExtension;
        String outputPath = Paths.get(this.outputFilePath, outputFileName).toString();
        log.info("Output file set to: {}", outputPath);

        // Add a unique run.id parameter to ensure job parameters are always unique which helps in preventing JobInstanceAlreadyCompleteException on subsequent runs with same file.
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                .addString(Constants.JOB_NAME, jobName)
                .addString(Constants.INPUT_FILE, inputFile)
                .addString(Constants.OUTPUT_FILE, outputPath)
                .addLong("run.id", System.currentTimeMillis());

        if (integrationUpperLimitFile != null && !integrationUpperLimitFile.trim().isEmpty()) {
            ParserUtils.checkingInputFileValidity(integrationUpperLimitFile);
            jobParametersBuilder.addString(Constants.INTEGRATION_UPPER_LIMIT_FILE, integrationUpperLimitFile);
        }

        JobParameters params = jobParametersBuilder.toJobParameters();
        log.debug("Job Parameters created: {}", params);
        return params;
    }

    private JobParameters createJobParamsForIndividualInputs(String jobName, String conditionFile, String programFile, String integrationUpperLimitFile,
                                                             String fileExtension) {
        File conditionFileAfterValidation = ParserUtils.checkingInputFileValidity(conditionFile);
        File programFileAfterValidation = ParserUtils.checkingInputFileValidity(programFile);

        ParserUtils.checkingInputFileValidity(integrationUpperLimitFile);

        String outputFileName = conditionFileAfterValidation.getName() + "_" + programFileAfterValidation.getName() + fileExtension;
        String outputPath = Paths.get(this.outputFilePath, outputFileName).toString();
        log.info("Output file set to: {}", outputPath);

        JobParameters params = new JobParametersBuilder()
                .addString(Constants.JOB_NAME, jobName)
                .addString(Constants.PRE_POST_CONDITION_FILE, conditionFile)
                .addString(Constants.DL_PROGRAM_FILE, programFile)
                .addString(Constants.INTEGRATION_UPPER_LIMIT_FILE, integrationUpperLimitFile)
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
            case DL_TO_D_REAL_OUTPUT_CONVERSION -> jobLauncher.run(loadDlToDRealConversionJob, jobParameters);
            case DL_TWO_FILES_COMBINING -> jobLauncher.run(loadDlCombiningTwoFilesJob, jobParameters);
            case DL_TO_D_REAL_OUTPUT_CONVERSION_FOR_INDIVIDUAL_INPUTS ->
                    jobLauncher.run(loadDlToDRealIndividualInputsConversionJob, jobParameters);
        }
    }
}