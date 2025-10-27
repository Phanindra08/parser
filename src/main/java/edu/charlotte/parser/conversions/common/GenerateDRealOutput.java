package edu.charlotte.parser.conversions.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class GenerateDRealOutput {
    public String createFileContent(String typeName, Set<String> identifiersSet, String dRealOutputContent) {
        Objects.requireNonNull(typeName, "Type name cannot be null for dReal output.");
        Objects.requireNonNull(dRealOutputContent, "dReal problem content cannot be null.");

        StringBuilder outputBuilder = new StringBuilder();
        this.appendFileHeader(outputBuilder, typeName);
        this.appendVariables(outputBuilder, identifiersSet);
        this.appendProgramOutput(outputBuilder, dRealOutputContent);
        this.appendFileFooter(outputBuilder);

        log.info("dReal output content successfully generated for the type: {}.", typeName);
        return outputBuilder.toString();
    }

    private void appendFileHeader(StringBuilder outputBuilder, String typeName) {
        log.debug("Type name for the header is '{}''.", typeName);
        outputBuilder.append("(set-logic QF_NRA)\n")
                .append("\n");
        log.info("Successfully appended file header for dReal output content.");
    }

    private void appendVariables(StringBuilder outputBuilder, Set<String> identifiersSet) {
        // Append program variables from identifiersSet
        if (identifiersSet != null && !identifiersSet.isEmpty()) {
            for (String identifier : identifiersSet) {
                if (identifier != null && !identifier.trim().isEmpty()) {
                    outputBuilder.append("(declare-fun ")
                            .append(identifier).append(" () Real)\n");
                } else
                    log.warn("Skipping null or empty identifier found in the identifiersSet.");
            }
        } else
            log.debug("No identifiers provided for the variables declaration section.");
        log.info("Successfully appended variables for dReal output content.");
    }

    private void appendProgramOutput(StringBuilder outputBuilder, String dRealOutputContent) {
        outputBuilder.append(dRealOutputContent).append("\n");
        log.info("Successfully appended output to the dReal output content.");
    }

    private void appendFileFooter(StringBuilder outputBuilder) {
        outputBuilder.append("(check-sat)\n")
                .append("(exit)");
        log.info("Successfully appended file footer for dReal output content.");
    }
}