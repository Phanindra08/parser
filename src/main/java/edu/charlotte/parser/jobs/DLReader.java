package edu.charlotte.parser.jobs;

import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class DLReader {

    @Bean
    @StepScope
    public ItemReader<String> dlFileReader(@Value("#{jobParameters['" + Constants.INPUT_FILE + "']}") String inputFile) {
        if (inputFile == null || inputFile.trim().isEmpty()) {
            log.error("Input file parameter is null or empty: {}", Constants.INPUT_FILE);
            throw new IllegalArgumentException("Input file path cannot be null or empty");
        }

        File file = new File(inputFile);
        if (!file.exists() || !file.isFile()) {
            log.error("Input file does not exist or is not a file: {}", inputFile);
            throw new IllegalArgumentException("Input file does not exist or is not a file: " + inputFile);
        }

        log.debug("Reading the entire DL Input file: {}", inputFile);
        return new SingleFileContentReader(inputFile);
    }

    private static class SingleFileContentReader implements ItemReader<String> {
        private final String filePath;
        private boolean read = false;

        public SingleFileContentReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String read() {
            if (read) {
                return null; // Return null to indicate no more items
            }
            try {
                // Read entire file into a single string
                String content = Files.readString(Paths.get(filePath));
                read = true;
                return content;
            } catch (Exception e) {
                log.error("Error reading file: {}", filePath, e);
                throw new RuntimeException("Failed to read file: " + filePath, e);
            }
        }
    }
}


