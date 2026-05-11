package edu.charlotte.parser.combining;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class GenerateCombinedOutput {
    public String createFileContent(String combinedOutputContent, String processorName) {
        Objects.requireNonNull(combinedOutputContent, "Combined output content cannot be null.");
        log.info("Combined {} output content successfully generated.", processorName);
        return combinedOutputContent;
    }
}