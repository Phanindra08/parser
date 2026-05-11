package edu.charlotte.parser.jobs.io;

import edu.charlotte.parser.dto.DlToDRealFileContentDTO;
import edu.charlotte.parser.exceptions.FileReadingException;
import edu.charlotte.parser.utils.Constants;
import edu.charlotte.parser.utils.ParserUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Configuration
@Slf4j
public class DlToDRealIndividualInputsFileReader {

    @Bean
    @StepScope
    public ItemReader<DlToDRealFileContentDTO> dlToDRealIndividualInputsItemReader(
            @Value("#{jobParameters['" + Constants.PRE_POST_CONDITION_FILE + "']}") String preAndPostConditionFile,
            @Value("#{jobParameters['" + Constants.DL_PROGRAM_FILE + "']}") String programFile,
            @Value("#{jobParameters['" + Constants.INTEGRATION_UPPER_LIMIT_FILE + "']}") String integrationUpperLimitFile) {

        Path preAndPostConditionFilePath = ParserUtils.getFilePath(preAndPostConditionFile);
        Path programFilePath = ParserUtils.getFilePath(programFile);
        Path upperLimitFilePath = ParserUtils.getFilePath(integrationUpperLimitFile);

        ParserUtils.checkingInputFileValidity(preAndPostConditionFilePath);
        ParserUtils.checkingInputFileValidity(programFilePath);
        ParserUtils.checkingInputFileValidity(upperLimitFilePath);

        log.debug("Reading the input files for DL to dReal conversion for individual inputs: '{}', '{}', '{}'.", preAndPostConditionFile, programFile,
                integrationUpperLimitFile);

        return new DlToDRealIndividualInputsContentReader(preAndPostConditionFilePath, programFilePath, upperLimitFilePath);
    }

    private static class DlToDRealIndividualInputsContentReader implements ItemReader<DlToDRealFileContentDTO> {

        private final Path preAndPostConditionFilePath;
        private final Path programFilePath;
        private final Path upperLimitFilePath;
        private boolean hasFileReadingCompleted;

        private DlToDRealIndividualInputsContentReader(Path preAndPostConditionFilePath, Path programFilePath, Path upperLimitFilePath) {
            this.preAndPostConditionFilePath = Objects.requireNonNull(preAndPostConditionFilePath,
                    "Pre and Post condition input file path for DlToDRealIndividualInputsContentReader cannot be null.");
            this.programFilePath = Objects.requireNonNull(programFilePath, "DL Program input file path for DlToDRealIndividualInputsContentReader cannot be null.");
            this.upperLimitFilePath = Objects.requireNonNull(upperLimitFilePath,
                    "File path containing upper limit value for DlToDRealIndividualInputsContentReader cannot be null.");
            this.hasFileReadingCompleted = false;
        }

        @Override
        public DlToDRealFileContentDTO read() {
            if (hasFileReadingCompleted) {
                log.debug("Content of the pre and post condition file, Program input file and integration upper limit file are already read.");
                return null;
            }

            try {
                List<String> conditionLines = Files.readAllLines(preAndPostConditionFilePath)
                        .stream()
                        .map(String::trim)
                        .filter(line -> !line.isBlank())
                        .toList();

                if (conditionLines.size() < 2)
                    throw new IllegalArgumentException(
                            "Condition file must contain precondition in the first line and postcondition in the second line.");

                String preCondition = extractConditionValue(conditionLines.getFirst(), true);
                String postCondition = extractConditionValue(conditionLines.get(1), false);
                String dlProgram = Files.readString(programFilePath).trim();
                String integrationUpperLimit = Files.readString(upperLimitFilePath).trim();

                if (dlProgram.isBlank()) {
                    throw new IllegalArgumentException("DL program cannot be empty.");
                }

                ParserUtils.validateNumericValue(integrationUpperLimit, Constants.FIELD_NAME_FOR_INTEGRATION_UPPER_LIMIT);

                String combinedDl = preCondition + Constants.SPACE +
                        Constants.DL_IMPLICATION_OPERATOR + Constants.SPACE +
                        Constants.DL_DIAMOND_MODALITY_OPENING_BRACKET + Constants.SPACE +
                        Constants.DL_OPEN_CURLY_BRACKETS + Constants.SPACE +
                        dlProgram + Constants.SPACE +
                        Constants.DL_AND_OPERATOR + Constants.SPACE +
                        Constants.TRUE +
                        Constants.DL_CLOSE_CURLY_BRACKETS + Constants.SPACE +
                        Constants.DL_DIAMOND_MODALITY_CLOSING_BRACKET + Constants.SPACE +
                        postCondition;

                hasFileReadingCompleted = true;

                log.info("Successfully created combined DL from individual input files.");
                log.debug("Combined DL input is: {}", combinedDl);

                log.info("Successfully read the contents from the files: '{}', '{}', '{}'.", this.preAndPostConditionFilePath, this.programFilePath, this.upperLimitFilePath);
                log.debug("Content of the input files are: '{}', '{}', '{}', '{}'.", preCondition, postCondition, dlProgram, integrationUpperLimit);

                return new DlToDRealFileContentDTO(combinedDl, integrationUpperLimit);

            } catch (IOException e) {
                log.error("Error reading one of the files: '{}', '{}', '{}'.", this.preAndPostConditionFilePath, this.programFilePath, this.upperLimitFilePath, e);
                throw new FileReadingException("Failed to read one of the files: " + this.preAndPostConditionFilePath + ", " + this.programFilePath + ", " +
                        this.upperLimitFilePath, e);
            }
        }

        private String extractConditionValue(String conditionLine, boolean isPreCondition) {
            String expectedPrefix = isPreCondition ? Constants.PRE_CONDITION_PREFIX : Constants.POST_CONDITION_PREFIX;

            if (conditionLine.toLowerCase().startsWith(expectedPrefix))
                return getString(conditionLine, expectedPrefix);

            throw new IllegalArgumentException(
                    isPreCondition ? "Condition file must contain precondition: <value>"
                            : "Condition file must contain postcondition: <value>");
        }

        private static @NonNull String getString(String conditionLineInLowerCase, String expectedPrefix) {
            int colonIndex = conditionLineInLowerCase.indexOf(Constants.COLON);
            if (colonIndex == -1)
                throw new IllegalArgumentException("The expected condition format is: " + expectedPrefix + " : <value>");

            String value = conditionLineInLowerCase.substring(colonIndex + 1).trim();
            if (value.isBlank())
                throw new IllegalArgumentException(expectedPrefix.substring(0, expectedPrefix.length() - 1) + " value cannot be empty.");
            return value;
        }
    }
}