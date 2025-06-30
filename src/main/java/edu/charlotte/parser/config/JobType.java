package edu.charlotte.parser.config;

import edu.charlotte.parser.utils.Constants;
import lombok.Getter;

@Getter
public enum JobType {
    DL_AST_GENERATION(Constants.JOBNAME_DL_AST_GENERATION, Constants.AST_GENERATION_EXTENSION),
    DL_TO_KEYMAERAX_OUTPUT_CONVERSION(Constants.JOBNAME_DL_TO_KEYMAERAX_OUTPUT_CONVERSION, Constants.KEYMAERAX_EXTENSION),
    REL_DL_AST_GENERATION(Constants.JOBNAME_REL_DL_AST_GENERATION, Constants.AST_GENERATION_EXTENSION),
    REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION(Constants.JOBNAME_REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION, Constants.KEYMAERAX_EXTENSION);

    private final String jobNameIdentifier;
    private final String fileExtension;

    JobType(String jobNameIdentifier, String fileExtension) {
        this.jobNameIdentifier = jobNameIdentifier;
        this.fileExtension = fileExtension;
    }

    // Helper method to get JobType from the Job Name identifier string
    public static JobType getJobType(String name) {
        for (JobType type : JobType.values()) {
            if (type.getJobNameIdentifier().equalsIgnoreCase(name))
                return type;
        }
        throw new IllegalArgumentException("Invalid Job: " + name);
    }
}
