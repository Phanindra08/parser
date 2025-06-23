package edu.charlotte.parser.jobs.reader_writer_jobs;

import edu.charlotte.parser.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FileWriter {
    @Bean
    @StepScope
    public FlatFileItemWriter<String> outputFileWriter(@Value("#{jobParameters['" + Constants.OUTPUT_FILE + "']}") String outputFile) {
        if (outputFile == null || outputFile.trim().isEmpty()) {
            log.error("Output file parameter '{}' is null or empty.", Constants.OUTPUT_FILE);
            throw new IllegalArgumentException("Output file path cannot be null or empty.");
        }

        Path outputFilePath = validateOutputFileAndDirectory(outputFile);

        log.info("Writing to the output File {}.", outputFilePath.getFileName());
        // Configure FlatFileItemWriter
        return new FlatFileItemWriterBuilder<String>()
                .name("outputFileWriter")
                .resource(new FileSystemResource(outputFilePath))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

    private Path validateOutputFileAndDirectory(String outputFile) {
        Path outputFilePath;
        try {
            outputFilePath = Paths.get(outputFile);
            log.debug("Output file path ({}) is valid.", outputFilePath);
        } catch (InvalidPathException e) {
            log.error("Invalid output file path: '{}'", outputFile, e);
            throw new IllegalArgumentException("Invalid output file path: " + outputFile, e);
        }

        // Ensuring the parent directory exists.
        Path parentDir = outputFilePath.getParent();
        // If the parent directory is null, means it's a root path or just a file name in current dir.
        if (parentDir != null) {
            try {
                log.debug("Checking or Creating the parent directory '{}' for the output file: {}", parentDir, outputFilePath.getFileName());
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                log.error("Failed to create parent directories for the output file: '{}'", parentDir, e);
                throw new IllegalArgumentException("Failed to create parent directories: " + parentDir, e);
            }

            // Checking if the parent directory is writable
            if (!Files.isWritable(parentDir)) {
                log.error("Parent directory '{}' for the output file '{}' is not writable.", parentDir, outputFilePath.getFileName());
                throw new IllegalArgumentException("Parent directory is not writable: " + parentDir);
            }
        } else {
            // If parent directory is null, it means the output file is a simple file name or in the current working directory. Check if current directory is writable.
            log.debug("Output file '{}' is a simple file name. Checking if the current directory is writable.", outputFilePath);
            if (!Files.isWritable(Paths.get("."))) {
                log.error("Current working directory is not writable for output file: '{}'", outputFilePath.getFileName());
                throw new IllegalArgumentException("Current working directory is not writable for output file: " + outputFilePath.getFileName());
            }
        }
        return outputFilePath;
    }
}