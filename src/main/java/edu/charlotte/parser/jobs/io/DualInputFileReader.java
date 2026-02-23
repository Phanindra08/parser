package edu.charlotte.parser.jobs.io;

import edu.charlotte.parser.dto.DualFileContentDTO;
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
public class DualInputFileReader {

    @Bean
    @StepScope
    public ItemReader<DualFileContentDTO> dualFileReader(
            @Value("#{jobParameters['" + Constants.INPUT_FILE1 + "']}") String inputFile1,
            @Value("#{jobParameters['" + Constants.INPUT_FILE2 + "']}") String inputFile2) {
        Path inputFilePath1 = ParserUtils.getFilePath(inputFile1);
        Path inputFilePath2 = ParserUtils.getFilePath(inputFile2);

        ParserUtils.checkingInputFileValidity(inputFilePath1);
        ParserUtils.checkingInputFileValidity(inputFilePath2);

        log.debug("Reading the input files: {}, {}", inputFile1, inputFile2);
        return new DualFileContentReader(inputFilePath1, inputFilePath2);
    }

    private static class DualFileContentReader implements ItemReader<DualFileContentDTO> {
        private final Path inputFilePath1;
        private final Path inputFilePath2;
        private boolean hasFileReadingCompleted;

        public DualFileContentReader(Path inputFilePath1, Path inputFilePath2) {
            this.inputFilePath1 = Objects.requireNonNull(inputFilePath1, "Input file path for DualFileContentReader cannot be null.");
            this.inputFilePath2 = Objects.requireNonNull(inputFilePath2, "Input file path for DualFileContentReader cannot be null.");
            this.hasFileReadingCompleted = false;
            log.debug("DualFileContentReader initialized for the files: '{}', '{}'.", inputFilePath1, inputFilePath2);
        }

        @Override
        public DualFileContentDTO read() {
            if (hasFileReadingCompleted) {
                log.debug("Content of the input file is already read.");
                return null; // Return null to indicate no more items
            }
            try {
                // Read entire file into a single string
                String contentOfFile1 = Files.readString(inputFilePath1);
                String contentOfFile2 = Files.readString(inputFilePath2);
                hasFileReadingCompleted = true;
                log.info("Successfully read the contents from the files: {}, {}", inputFilePath1, inputFilePath2);
                log.debug("Content of the files are: {}, {}", contentOfFile1, contentOfFile2);
                return new DualFileContentDTO(contentOfFile1, contentOfFile2);
            } catch (IOException e) {
                log.error("Error reading either the file {} or the file {}", inputFilePath1, inputFilePath2, e);
                throw new FileReadingException("Failed to read either of the files: " + inputFilePath1 + inputFilePath2, e);
            }
        }
    }
}