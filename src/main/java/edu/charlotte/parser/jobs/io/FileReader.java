package edu.charlotte.parser.jobs.reader_writer_jobs;

import edu.charlotte.parser.exceptions.FileReadingException;
import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FileReader {

    @Bean
    @StepScope
    public ItemReader<String> inputFileReader(@Value("#{jobParameters['" + Constants.INPUT_FILE + "']}") String inputFile) {
        if (inputFile == null || inputFile.trim().isEmpty()) {
            log.error("Input file path parameter is null or empty. Parameter value: {}", Constants.INPUT_FILE);
            throw new IllegalArgumentException("Input file path cannot be null or empty.");
        }

        Path inputFilePath;
        try {
            inputFilePath = Paths.get(inputFile);
            log.debug("Input file path ({}) is valid.", inputFilePath);
        } catch (InvalidPathException e) {
            log.error("Invalid input file path: {}", inputFile, e);
            throw new IllegalArgumentException("Invalid input file path: " + inputFile, e);
        }


        if (!Files.exists(inputFilePath)) {
            log.error("Input file does not exist or is not a file: {}", inputFile);
            throw new IllegalArgumentException("Input file does not exist or is not a file: " + inputFile);
        }

        log.debug("Reading the input file: {}", inputFile);
        return new SingleFileContentReader(inputFilePath);
    }

    private static class SingleFileContentReader implements ItemReader<String> {
        private final Path inputFilePath;
        private boolean hasFileReadingCompleted;

        public SingleFileContentReader(Path inputFilePath) {
            this.inputFilePath = inputFilePath;
            this.hasFileReadingCompleted = false;
        }

        @Override
        public String read() {
            if (hasFileReadingCompleted) {
                log.debug("Content of the input file is already read.");
                return null; // Return null to indicate no more items
            }
            try {
                // Read entire file into a single string
                String content = Files.readString(inputFilePath);
                hasFileReadingCompleted = true;
                log.info("Successfully read the contents from the file: {}", inputFilePath);
                log.debug("Content of the file: {}", content);
                return content;
            } catch (IOException e) {
                log.error("Error reading the file: {}", inputFilePath, e);
                throw new FileReadingException("Failed to read the file: " + inputFilePath, e);
            }
        }
    }
}