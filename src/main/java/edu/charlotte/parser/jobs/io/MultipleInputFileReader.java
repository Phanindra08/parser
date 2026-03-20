package edu.charlotte.parser.jobs.io;

import edu.charlotte.parser.dto.MultipleFileContentDTO;
import edu.charlotte.parser.exceptions.FileReadingException;
import edu.charlotte.parser.utils.Constants;
import edu.charlotte.parser.utils.ParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Configuration
@Slf4j
public class MultipleInputFileReader {

    @Bean
    @StepScope
    public ItemReader<MultipleFileContentDTO> multipleFileReader(
            @Value("#{jobParameters['" + Constants.PRE_AND_POST_CONDITION_INPUT_FILE + "']}") String inputConditionsFile,
            @Value("#{jobParameters['" + Constants.INPUT_FILE1 + "']}") String inputFile1,
            @Value("#{jobParameters['" + Constants.INPUT_FILE2 + "']}") String inputFile2,
            @Value("#{jobParameters['" + Constants.CONSTANT_VALUE + "']}") float constantValue) {
        Path inputConditionsFilePath = ParserUtils.getFilePath(inputConditionsFile);
        Path inputFilePath1 = ParserUtils.getFilePath(inputFile1);
        Path inputFilePath2 = ParserUtils.getFilePath(inputFile2);

        ParserUtils.checkingInputFileValidity(inputConditionsFilePath);
        ParserUtils.checkingInputFileValidity(inputFilePath1);
        ParserUtils.checkingInputFileValidity(inputFilePath2);

        log.debug("Reading the input files: {}, {}, {}", inputConditionsFile, inputFile1, inputFile2);
        return new MultipleFileContentReader(inputConditionsFilePath, inputFilePath1, inputFilePath2, constantValue);
    }

    private static class MultipleFileContentReader implements ItemReader<MultipleFileContentDTO> {
        private final Path inputConditionsFilePath;
        private final Path inputFilePath1;
        private final Path inputFilePath2;
        private final float constantValue;
        private boolean hasFileReadingCompleted;

        public MultipleFileContentReader(Path inputConditionsFilePath, Path inputFilePath1, Path inputFilePath2, float constantValue) {
            this.inputConditionsFilePath = Objects.requireNonNull(inputConditionsFilePath, "Input file path for MultipleFileContentReader cannot be null.");
            this.inputFilePath1 = Objects.requireNonNull(inputFilePath1, "Input file path for MultipleFileContentReader cannot be null.");
            this.inputFilePath2 = Objects.requireNonNull(inputFilePath2, "Input file path for MultipleFileContentReader cannot be null.");
            this.constantValue = constantValue;
            this.hasFileReadingCompleted = false;
            log.debug("MultipleFileContentReader initialized for the files: '{}', '{}', '{}'.",
                    inputConditionsFilePath, inputFilePath1, inputFilePath2);
        }

        @Override
        public MultipleFileContentDTO read() {
            if (hasFileReadingCompleted) {
                log.debug("Content of the input file is already read.");
                return null; // Return null to indicate no more items
            }
            try {
                // Read entire file into a single string
                String contentOfConditionsFile = Files.readString(inputConditionsFilePath);
                String contentOfFile1 = Files.readString(inputFilePath1);
                String contentOfFile2 = Files.readString(inputFilePath2);

                hasFileReadingCompleted = true;
                log.info("Successfully read the contents from the files: {}, {}, {}", inputConditionsFilePath, inputFilePath1, inputFilePath2);
                log.debug("Content of the files are: {}, {}, {}", contentOfConditionsFile, contentOfFile1, contentOfFile2);
                return new MultipleFileContentDTO(contentOfConditionsFile, contentOfFile1, contentOfFile2, constantValue);
            } catch (IOException e) {
                log.error("Error reading one of the files {}, {}, {}", inputConditionsFilePath, inputFilePath1, inputFilePath2, e);
                throw new FileReadingException("Failed to read one of the files: " + inputConditionsFilePath + inputFilePath1 + inputFilePath2, e);
            }
        }
    }
}