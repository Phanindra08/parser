package edu.charlotte.parser.jobs.io;

import edu.charlotte.parser.dto.DlToDRealFileContentDTO;
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
public class DlToDRealInputFileReader {

    @Bean
    @StepScope
    public ItemReader<DlToDRealFileContentDTO> dlToDRealFileReader(
            @Value("#{jobParameters['" + Constants.INPUT_FILE + "']}") String inputFile,
            @Value("#{jobParameters['" + Constants.INTEGRATION_UPPER_LIMIT_FILE + "']}") String integrationUpperLimitFile) {

        Path inputFilePath = ParserUtils.getFilePath(inputFile);
        ParserUtils.checkingInputFileValidity(inputFilePath);

        Path integrationUpperLimitFilePath = null;
        if (integrationUpperLimitFile != null && !integrationUpperLimitFile.isBlank()) {
            integrationUpperLimitFilePath = ParserUtils.getFilePath(integrationUpperLimitFile);
            ParserUtils.checkingInputFileValidity(integrationUpperLimitFilePath);
        }

        log.debug("Reading the input files for DL to dReal conversion: '{}', '{}'.", inputFile, integrationUpperLimitFile);

        return new DlToDRealContentReader(inputFilePath, integrationUpperLimitFilePath);
    }

    private static class DlToDRealContentReader implements ItemReader<DlToDRealFileContentDTO> {
        private final Path inputFilePath;
        private final Path integrationUpperLimitFilePath;
        private boolean hasFileReadingCompleted;

        public DlToDRealContentReader(Path inputFilePath, Path integrationUpperLimitFilePath) {
            this.inputFilePath = Objects.requireNonNull(inputFilePath,
                    "Input file path for DlToDRealContentReader cannot be null.");
            this.integrationUpperLimitFilePath = integrationUpperLimitFilePath;
            this.hasFileReadingCompleted = false;

            log.debug("DlToDRealContentReader initialized for files: inputFile='{}', integrationUpperLimitFile='{}'.",
                    this.inputFilePath, this.integrationUpperLimitFilePath);
        }

        @Override
        public DlToDRealFileContentDTO read() {
            if (this.hasFileReadingCompleted) {
                log.debug("Content of the DL input file and integration upper limit file are already read.");
                return null;
            }

            try {
                String dlInputContent = Files.readString(this.inputFilePath);
                String integrationUpperLimitContent = null;

                if (this.integrationUpperLimitFilePath != null) {
                    integrationUpperLimitContent = Files.readString(this.integrationUpperLimitFilePath).trim();
                    ParserUtils.validateNumericValue(integrationUpperLimitContent, Constants.FIELD_NAME_FOR_INTEGRATION_UPPER_LIMIT);
                }

                this.hasFileReadingCompleted = true;

                log.info("Successfully read the contents from the files: '{}', '{}'.", this.inputFilePath, this.integrationUpperLimitFilePath);
                log.debug("Content of the input files are: '{}', '{}'.", dlInputContent, integrationUpperLimitContent);

                return new DlToDRealFileContentDTO(dlInputContent, integrationUpperLimitContent);
            } catch (IOException e) {
                log.error("Error reading one of the files: '{}', '{}'.", this.inputFilePath, this.integrationUpperLimitFilePath, e);
                throw new FileReadingException("Failed to read one of the files: " + this.inputFilePath + ", " + this.integrationUpperLimitFilePath, e);
            }
        }
    }
}