package edu.charlotte.parser.utils;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
    private Constants() {
    }

    public static final Map<String, Integer> NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS = new HashMap<>();
    public static final Map<String, Integer> DL_SYNTAX_POSITIONS_AFTER_CONVERSION = new HashMap<>();

    public static final String JOBNAME_DL_AST_GENERATION = "DL_AST_GENERATION";
    public static final String JOBNAME_DL_TO_KEYMAERAX_OUTPUT_CONVERSION = "DL_TO_KEYMAERAX_OUTPUT";
    public static final String JOBNAME_REL_DL_AST_GENERATION = "REL_DL_AST_GENERATION";
    public static final String JOBNAME_REL_DL_TO_KEYMAERAX_OUTPUT_CONVERSION = "REL_DL_TO_KEYMAERAX_OUTPUT";
    public static final String JOBNAME_D_REAL_AST_GENERATION = "D_REAL_AST_GENERATION";
    public static final String JOBNAME_DL_TO_D_REAL_OUTPUT_CONVERSION = "DL_TO_D_REAL_OUTPUT";
    public static final String JOBNAME_REL_DL_TWO_FILES_COMBINING = "REL_DL_TWO_FILES_COMBINING";
    public static final String JOBNAME_DL_TWO_FILES_COMBINING = "DL_TWO_FILES_COMBINING";

    public static final String AST_GENERATION_EXTENSION = "_AST.txt";
    public static final String KEYMAERAX_EXTENSION = ".kyx";
    public static final String SMT_EXTENSION = ".smt2";
    public static final String TWO_FILE_COMBINING_PREFIX = "Combining_";

    public static final String INPUT_FILE = "input.file";
    public static final String INPUT_FILE1 = "input.file1";
    public static final String INPUT_FILE2 = "input.file2";
    public static final String CONSTANT_VALUE_FILE = "constant.value";
    public static final String OUTPUT_FILE = "output.file";
    public static final String JOB_NAME = "job.name";

    public static final String ERROR_MESSAGE_FOR_MISSING_JOB_PARAMETERS = "Missing required job parameter. " +
            "Use --job.name=<jobName>";
    public static final String ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETER = "Missing required input parameter. " +
            "Use --input.file=<inputFile>";
    public static final String ERROR_MESSAGE_FOR_MISSING_INPUT_PARAMETERS = "Missing required input parameters. " +
            "Use --input.file.for.condition=<inputFileForCondition> --input.file1=<inputFile>, --input.file2=<inputFile> and --constant.value=<constantValue>";

    public static final String AST_NODE_DL_PROGRAM = "DLProgram";
    public static final String AST_NODE_DL_FORMULA = "Formula";
    public static final String AST_NODE_DL_PROGRAM_CONTEXT = "Program";
    public static final String AST_NODE_DL_DIFFERENTIAL_EQUATION = "DifferentialEquation";
    public static final String AST_NODE_DL_BINARY_EXPRESSION = "BinaryExpression";
    public static final String AST_NODE_DL_TERM_WITH_PARENTHESES = "TermWithParentheses";

    public static final String AST_NODE_REL_DL_PROGRAM = "RelationalDLProgram";
    public static final String AST_NODE_REL_DL_FORMULA = "Relational Formula";
    public static final String AST_NODE_REL_DL_PROGRAM_CONTEXT = "Relational Program";
    public static final String AST_NODE_REL_DL_TERM = "Relational Term";

    public static final String AST_NODE_D_REAL_PROGRAM = "DRealProgram";
    public static final String AST_NODE_D_REAL_COMMAND = "DReal Command";
    public static final String AST_NODE_D_REAL_RESPONSE = "DReal Response";
    public static final String AST_NODE_D_REAL_SCRIPT = "DReal Script";

    public static final char PROGRAM_CONSIDERED_L = 'L';
    public static final char PROGRAM_CONSIDERED_R = 'R';
    public static final char PROGRAM_CONSIDERED_G = 'G';
    public static final String LEFT_PROGRAM = "@L";
    public static final String RIGHT_PROGRAM = "@R";

    public static final String REL_DL_OPEN_BRACKETS = "(#";
    public static final String REL_DL_CLOSE_BRACKETS = ")#";
    public static final String REL_DL_COMMA = ",#";
    public static final String REL_DL_ASSIGNMENT_OPERATOR = ":=#";

    public static final String DL_OPEN_BRACKETS = "(";
    public static final String DL_CLOSE_BRACKETS = ")";
    public static final String DL_OPEN_CURLY_BRACKETS = "{";
    public static final String DL_CLOSE_CURLY_BRACKETS = "}";
    public static final String DL_EQUAL_OPERATOR = "==";
    public static final String DL_AND_OPERATOR = "&&";
    public static final String DL_OR_OPERATOR = "||";
    public static final String DL_BOX_MODALITY_OPENING_BRACKET = "[";
    public static final String DL_BOX_MODALITY_CLOSING_BRACKET = "]";
    public static final String DL_DIAMOND_MODALITY_OPENING_BRACKET = "<<";
    public static final String DL_DIAMOND_MODALITY_CLOSING_BRACKET = ">>";
    public static final String DL_ASSIGNMENT_OPERATOR = ":=";
    public static final String DL_COMMA = ",";
    public static final String DL_GREATER_THAN_OPERATOR = ">";
    public static final String DL_GREATER_THAN_AND_EQUAL_TO_OPERATOR = ">=";
    public static final String DL_LESS_THAN_OPERATOR = "<";
    public static final String DL_LESS_THAN_AND_EQUAL_TO_OPERATOR = "<=";
    public static final String DL_SEMI_COLON = ";";
    public static final String DL_SEQUENTIAL_COMPOSITION = ";";
    public static final String DL_NOT_EQUAL_OPERATOR = "!=";
    public static final String DL_NOT_OPERATOR = "!";
    public static final String DL_MULTIPLICATION_OPERATOR = "*";
    public static final String DL_ADDITION_OPERATOR = "+";
    public static final String DL_SUBTRACTION_OPERATOR = "-";
    public static final String DL_DIVISION_OPERATOR = "/";
    public static final String DL_TERNARY_OPERATOR = "?";
    public static final String DL_IMPLICATION_OPERATOR = "->";
    public static final String DL_BI_IMPLICATION_OPERATOR = "<->";
    public static final String DL_IDENTIFIERS_REGEX = "[a-zA-Z][a-zA-Z0-9]*";
    public static final int DL_NOT_FORMULA_SIZE = 4;

    public static final String DL_SEQUENTIAL_COMPOSITION_PROGRAM1 = "DLSequentialCompositionProgram1";
    public static final String DL_SEQUENTIAL_COMPOSITION_PROGRAM2 = "DLSequentialCompositionProgram2";
    public static final String DL_TERNARY_OPERATOR_FORMULA = "DLTernaryOperatorFormula";
    public static final String DL_ASSIGNMENT_OPERATOR_IDENTIFIER = "DLAssignmentOperatorIdentifier";
    public static final String DL_ASSIGNMENT_OPERATOR_TERM = "DLAssignmentOperatorTerm";
    public static final String DL_COMPARISON_OPERATOR_TERM1 = "DLComparisonOperatorTerm1";
    public static final String DL_COMPARISON_OPERATOR_TERM2 = "DLComparisonOperatorTerm2";
    public static final String DL_LOGICAL_OPERATOR_FORMULA1 = "DLLogicalOperatorFormula1";
    public static final String DL_LOGICAL_OPERATOR_FORMULA2 = "DLLogicalOperatorFormula2";
    public static final String DL_START_POSITION_OF_DIFFERENTIAL_EQUATION_IN_PROGRAM = "DLDifferentialEquationStartPositionInProgram";
    public static final String DL_POSITION_OF_AND_OPERATOR_IN_DIFFERENTIAL_EQUATION_PROGRAM_FROM_LAST = "DLAndOperatorInDifferentialEquationProgramFromLast";
    public static final String DL_POSITION_OF_FORMULA_IN_DIFFERENTIAL_EQUATION_PROGRAM_FROM_LAST = "DFormulaInDifferentialEquationProgramFromLast";

    public static final String DL_OPERATORS_POSITION_WITH_TWO_OPERANDS = "DL_OPERATORS_WITH_TWO_OPERANDS";
    public static final String DL_PROGRAM_POSITION_IN_MODALITY_OPERATOR = "DL_PROGRAM_POSITION_IN_MODALITY_OPERATOR";
    public static final String DL_FORMULA_POSITION_IN_MODALITY_OPERATOR = "DL_FORMULA_POSITION_IN_MODALITY_OPERATOR";
    public static final String DL_FORMULA_POSITION_IN_NOT_OPERATOR = "DL_FORMULA_POSITION_IN_NOT_OPERATOR";

    public static final String AST_GENERATION_PROCESS_SUFFIX = " Ast Generation Process";
    public static final String KEYMAERAX_OUTPUT_CONVERSION_SUFFIX = " to KeYmaeraX Output Conversion Process";
    public static final String MESSAGE_TO_COMBINE_TWO_INPUTS = "Combining two %s inputs into a single file";
    public static final String D_REAL_OUTPUT_CONVERSION_SUFFIX = " to DReal Output Conversion Process";

    public static final String DIFFERENTIAL_DYNAMIC_LOGIC = "Differential Dynamic Logic";
    public static final String RELATIONAL_DYNAMIC_LOGIC = "Relational Dynamic Logic";
    public static final String D_REAL = "dReal";

    public static final String NOT_FOR_D_REAL = "not";
    public static final String OR_FOR_D_REAL = "or";
    public static final String AND_FOR_D_REAL = "and";
    public static final String EQUAL_OPERATOR_FOR_D_REAL = "=";
    public static final String IMPLICATION_OPERATOR_FOR_D_REAL = "=>";
    public static final String PROGRAM_IN_D_REAL = "Program_In_DReal";
    public static final String FORMULA_IN_D_REAL = "Formula_In_DReal";
    public static final String D_REAL_OPENING_BRACKET = "(";
    public static final String D_REAL_CLOSING_BRACKET = ")";
    public static final String TIME = "time";
    public static final String DIFFERENTIAL_EQUATION = "flow_1";

    public static final String LOGICAL_OPERATOR_FOR_DL_LENGTH = "Length of dReal with Logical Operator";
    public static final String DL_OPERATORS_WITH_TWO_OPERANDS_LENGTH = "Length of DL Program with two Operands";
    public static final String NOT_FOR_D_REAL_LENGTH = "Length of dReal with Not Operator";
    public static final String DL_DIAMOND_MODALITY_LENGTH = "Length of DL Program with Diamond Modality Operator";
    public static final String DL_BOX_MODALITY_LENGTH = "Length of DL Program with Box Modality Operator";
    public static final String DL_DIFFERENTIAL_EQUATION_LENGTH = "Length of DL Program with differential equation";

    public static final String LOG_MESSAGE_FOR_APPENDING_NODE_VALUE_TO_D_REAL_OUTPUT = "Appended the node value '{}' to dReal output.";
    public static final String VERIFICATION_FAILED = "failed";
    public static final String VERIFICATION_FALSE = "false";

    public static final String EOF = "<EOF>";
    public static final String EMPTY_STRING = "";
    public static final String NEXT_LINE = "\n";
    public static final String TAB = "\t";
    public static final String SPACE = " ";
    public static final Character DASH = '\'';
    public static final String REG_EXP_FOR_NUMBERS_AT_END = "^(.*?)(\\d+)$";

    static {
        NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.put(Constants.LOGICAL_OPERATOR_FOR_DL_LENGTH, 3);
        NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.put(Constants.DL_OPERATORS_WITH_TWO_OPERANDS_LENGTH, 3);
        NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.put(Constants.NOT_FOR_D_REAL_LENGTH, 4);
        NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.put(Constants.DL_DIAMOND_MODALITY_LENGTH, 4);
        NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.put(Constants.DL_BOX_MODALITY_LENGTH, 4);
        NUMBER_OF_CHILD_NODES_FOR_DL_OPERATORS.put(Constants.DL_DIFFERENTIAL_EQUATION_LENGTH, 5);

        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.NOT_FOR_D_REAL, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_FORMULA_POSITION_IN_NOT_OPERATOR, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_OPERATORS_POSITION_WITH_TWO_OPERANDS, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_LOGICAL_OPERATOR_FORMULA1, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_LOGICAL_OPERATOR_FORMULA2, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.IMPLICATION_OPERATOR_FOR_D_REAL, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_BOX_MODALITY_OPENING_BRACKET, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_BOX_MODALITY_CLOSING_BRACKET, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_DIAMOND_MODALITY_OPENING_BRACKET, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_DIAMOND_MODALITY_CLOSING_BRACKET, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_PROGRAM_POSITION_IN_MODALITY_OPERATOR, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_FORMULA_POSITION_IN_MODALITY_OPERATOR, 3);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.EQUAL_OPERATOR_FOR_D_REAL, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_SEQUENTIAL_COMPOSITION, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_SEQUENTIAL_COMPOSITION_PROGRAM1, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_SEQUENTIAL_COMPOSITION_PROGRAM2, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_TERNARY_OPERATOR, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_TERNARY_OPERATOR_FORMULA, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_START_POSITION_OF_DIFFERENTIAL_EQUATION_IN_PROGRAM, 1);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_POSITION_OF_AND_OPERATOR_IN_DIFFERENTIAL_EQUATION_PROGRAM_FROM_LAST, 3);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_POSITION_OF_FORMULA_IN_DIFFERENTIAL_EQUATION_PROGRAM_FROM_LAST, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_ASSIGNMENT_OPERATOR_IDENTIFIER, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_ASSIGNMENT_OPERATOR_TERM, 2);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_COMPARISON_OPERATOR_TERM1, 0);
        DL_SYNTAX_POSITIONS_AFTER_CONVERSION.put(Constants.DL_COMPARISON_OPERATOR_TERM2, 2);
    }
}