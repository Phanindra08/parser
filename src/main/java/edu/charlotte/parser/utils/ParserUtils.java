package edu.charlotte.parser.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public final class ParserUtils {

    private static final int DEFAULT_TRUNCATION_LENGTH = 50;
    private ParserUtils() {}

    public static String formatInputForLogging(String input, int maxLength) {
        if (input == null)
            return "[null]";

        if (maxLength < 0) {
            maxLength = 0;
        }

        if (input.length() > maxLength) {
            int effectiveLength = Math.max(0, maxLength - 3); // Leave room for "..."
            return input.substring(0, effectiveLength) + "...";
        } else
            return input;
    }

    public static String formatInputForLogging(String input) {
        return formatInputForLogging(input, ParserUtils.DEFAULT_TRUNCATION_LENGTH);
    }

    public static void validateInputFilePathIsNotNull(String inputFilePath) {
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            log.error("Input file path ({}) cannot be null or empty.", inputFilePath);
            throw new IllegalArgumentException("Input file path cannot be null or empty.");
        }
    }

    public static File checkingInputFileValidity(String inputFile) {
        File input = new File(inputFile);
        if (!input.exists() || !input.isFile()) {
            log.error("Input file ({}) does not exist or is not a file.", inputFile);
            throw new IllegalArgumentException("Input file does not exist or is not a file: " + inputFile);
        }
        return input;
    }

    public static void checkingInputFileValidity(Path inputFilePath) {
        if (!Files.exists(inputFilePath)) {
            log.error("Input file does not exist or is not a file: {}", inputFilePath.getFileName());
            throw new IllegalArgumentException("Input file does not exist or is not a file: " + inputFilePath.getFileName());
        }
    }

    public static Path getFilePath(String inputFile) {
        Path inputFilePath;
        try {
            inputFilePath = Paths.get(inputFile);
            log.debug("Input file path ({}) is valid.", inputFilePath);
        } catch (InvalidPathException e) {
            log.error("Invalid input file path: {}", inputFile, e);
            throw new IllegalArgumentException("Invalid input file path: " + inputFile, e);
        }
        return inputFilePath;
    }

    public static boolean checkingArrayInputIsNotEmpty(String[] inputs, int length) {
        if(inputs.length < length)
            return false;
        for(String input: inputs) {
            if(input == null || input.isEmpty())
                return false;
        }
        return true;
    }
}