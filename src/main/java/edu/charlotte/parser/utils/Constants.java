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

    public static final String AST_NODE_REL_DL_PROGRAM = "RelationalDLProgram";
    public static final String AST_NODE_REL_DL_FORMULA = "Relational Formula";
    public static final String AST_NODE_REL_DL_PROGRAM_CONTEXT = "Relational Program";
    public static final String AST_NODE_REL_DL_TERM = "Relational Term";

    public static final char PROGRAM_CONSIDERED_L = 'L';
    public static final char PROGRAM_CONSIDERED_R = 'R';
    public static final char PROGRAM_CONSIDERED_G = 'G';
    public static final String LEFT_PROGRAM = "#L";
    public static final String RIGHT_PROGRAM = "#R";

    public static final String AST_GENERATION_PROCESS_SUFFIX = " Ast Generation Process";
    public static final String DIFFERENTIAL_DYNAMIC_LOGIC = "Differential Dynamic Logic";
    public static final String RELATIONAL_DYNAMIC_LOGIC = "Relational Dynamic Logic";
    public static final String KEYMAERAX_OUTPUT_CONVERSION_SUFFIX = " to KeYMaeraX Output Conversion Process";
}