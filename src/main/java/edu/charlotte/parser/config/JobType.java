package edu.charlotte.parser.config;

import edu.charlotte.parser.utils.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum JobType {
    DL_AST_GENERATION(Constants.JOBNAME_DL_AST_GENERATION, Constants.AST_GENERATION_EXTENSION),
    DL_TO_KEYMAERAX_OUTPUT_CONVERSION(Constants.JOBNAME_DL_TO_KEYMAERAX_OUTPUT_CONVERSION, Constants.KEYMAERAX_EXTENSION),
    REL_DL_AST_GENERATION(Constants.JOBNAME_REL_DL_AST_GENERATION, Constants.AST_GENERATION_EXTENSION),
    REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION(Constants.JOBNAME_REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION, Constants.KEYMAERAX_EXTENSION),
    D_REAL_AST_GENERATION(Constants.JOBNAME_D_REAL_AST_GENERATION, Constants.AST_GENERATION_EXTENSION),
    DL_TO_D_REAL_OUTPUT_CONVERSION(Constants.JOBNAME_DL_TO_D_REAL_OUTPUT_CONVERSION, Constants.SMT_EXTENSION),
    REL_DL_TWO_FILES_COMBINING(Constants.JOBNAME_REL_DL_TWO_FILES_COMBINING, Constants.TWO_FILE_COMBINING_PREFIX),
    DL_TO_D_REAL_OUTPUT_CONVERSION_FOR_INDIVIDUAL_INPUTS(Constants.JOBNAME_DL_TO_D_REAL_OUTPUT_FOR_INDIVIDUAL_INPUTS, Constants.SMT_EXTENSION),
    DL_TWO_FILES_COMBINING(Constants.JOBNAME_DL_TWO_FILES_COMBINING, Constants.EMPTY_STRING);

    private final String jobNameIdentifier;
    private final String fileExtension;

    JobType(String jobNameIdentifier, String fileExtension) {
        this.jobNameIdentifier = jobNameIdentifier;
        this.fileExtension = fileExtension;
    }

    // Helper method to get JobType from the Job Name identifier string
    public static JobType getJobType(String name) {
        log.info("The job name is {}", name);
        for (JobType type : JobType.values()) {
            if (type.getJobNameIdentifier().equalsIgnoreCase(name))
                return type;
        }
        throw new IllegalArgumentException("Invalid Job: " + name);
    }
}
