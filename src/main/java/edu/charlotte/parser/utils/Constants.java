package edu.charlotte.parser.utils;

public final class Constants {
    private Constants() {}
    public static final String JOBNAME_DL_AST_GENERATION = "DL_AST_GENERATION";
    public static final String JOBNAME_DL_TO_KEYMAERAX_OUTPUT_CONVERSION = "DL_TO_KEYMAERAX_OUTPUT";
    public static final String JOBNAME_REL_DL_AST_GENERATION = "REL_DL_AST_GENERATION";
    public static final String JOBNAME_REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION = "REL_DL_TO_KEYMAERAX_OUTPUT";

    public static final String AST_GENERATION_EXTENSION = "_AST.txt";
    public static final String KEYMAERAX_EXTENSION = ".kyx";

    public static final String INPUT_FILE = "inputFile";
    public static final String OUTPUT_FILE = "outputFile";

    public static final String ERROR_MESSAGE_FOR_MISSING_JOB_PARAMETERS = "Missing required job parameters. " +
            "Use --job.name=<jobName> and --input.file=<inputFile>";

    public static final String AST_NODE_DL_PROGRAM = "DLProgram";
    public static final String AST_NODE_DL_FORMULA = "Formula";
    public static final String AST_NODE_DL_PROGRAM_CONTEXT = "Program";
    public static final String AST_NODE_DL_BINARY_EXPRESSION = "BinaryExpression";
}