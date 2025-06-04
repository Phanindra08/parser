package edu.charlotte.parser.config;

import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class BatchConfig implements CommandLineRunner {

//    @Autowired
//    private Job loadDLASTGenerationJob;

    @Autowired
    Job loadDLToKeYMaeraXConversionJob;

    @Autowired
    JobLauncher jobLauncher;

    @Value("${dl-output}")
    String outputFilePath;

    @Override
    public void run(String ...args) throws Exception {
        if(args.length < 2)
            throw new JobParametersNotFoundException("Invalid number of Job parameters.");

        long start = System.currentTimeMillis();
        String jobName = null;
        String inputFile = null;

        // If the argument is passed as --job.name= and --input.file=
        for (String arg : args) {
            if (arg.startsWith("--job.name=")) {
                jobName = arg.substring("--job.name=".length());
            } else if (arg.startsWith("--input.file=")) {
                inputFile = arg.substring("--input.file=".length());
            }
        }

        // Fallback to raw arguments if not found
        if (jobName == null || inputFile == null) {
            jobName = args[args.length - 2];
            inputFile = args[args.length - 1];
        }
        checkArgumentsValidity(jobName, inputFile);
        executeJob(jobName, inputFile);
        if (log.isInfoEnabled())
            log.info("Job execution took {} ms", System.currentTimeMillis() - start);
    }

    private void checkArgumentsValidity(String jobName, String inputFile) {
        if (jobName == null || jobName.trim().isEmpty()) {
            log.error("Job name is null or empty.");
            System.exit(-1);
        }
        if(inputFile == null || inputFile.trim().isEmpty()) {
            log.error("Input Path is not found for the file: {}", inputFile);
            System.exit(-1);
        }
        log.info("Job Name to be parsed: {}, Input File to be parsed: {}", jobName, inputFile);
    }

    private JobParameters createJobParams(String inputFile, boolean isHavingOutputFile) {
        File input = new File(inputFile);
        if (!input.exists() || !input.isFile()) {
            log.error("Input file does not exist: {}", inputFile);
            throw new IllegalArgumentException("Input file does not exist: " + inputFile);
        }
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString(Constants.INPUT_FILE, inputFile);
        if (isHavingOutputFile) {
            String outputFile = input.getName() + ".kyx";
            builder.addString(Constants.OUTPUT_FILE, this.outputFilePath + outputFile);
            log.info("Output file set to: {}", outputFile);
        }
        JobParameters params = builder.toJobParameters();
        log.info("JobParameters created: {}", params);
        return params;
    }

    private void executeJob(String jobName, String inputFile) throws Exception {
        JobParameters jobParameters;
        switch(jobName.toUpperCase()) {
            case Constants.JOBNAME_DL_AST_GENERATION -> {
                log.info("Starting Job: {}", Constants.JOBNAME_DL_AST_GENERATION);
                jobParameters = createJobParams(inputFile, false);
//              jobLauncher.run(loadDLASTGenerationJob, jobParameters);
            }
            case Constants.JOBNAME_DL_TO_KEYMAERAX_OUTPUT_CONVERSION -> {
                log.info("Starting Job: {}", Constants.JOBNAME_DL_TO_KEYMAERAX_OUTPUT_CONVERSION);
                jobParameters = createJobParams(inputFile, true);
                log.debug("Job Parameters are: {}", jobParameters);
                jobLauncher.run(loadDLToKeYMaeraXConversionJob, jobParameters);
            }
            default -> {
                log.error("Invalid Job: {}", jobName.toUpperCase());
                System.exit(-1);
            }
        }
    }
}
