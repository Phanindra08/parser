package edu.charlotte.parser.conversions.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class GenerateKeYmaeraXOutput {
    public String createFileContent(String typeName, Set<String> identifiersSet, String keYmaeraXOutputContent) {
        Objects.requireNonNull(typeName, "Type name cannot be null for KeYmaeraX output.");
        Objects.requireNonNull(keYmaeraXOutputContent, "KeYmaeraX problem content cannot be null.");

        StringBuilder outputBuilder = new StringBuilder();
        this.appendFileHeader(outputBuilder, typeName);
        this.appendProgramVariable(outputBuilder, identifiersSet);
        this.appendProblemSection(outputBuilder, keYmaeraXOutputContent);
        outputBuilder.append("End.");

        log.info("KeYmaeraX output content successfully generated for the type: {}.", typeName);
        return outputBuilder.toString();
    }

    private void appendFileHeader(StringBuilder outputBuilder, String typeName) {
        String uniqueID = UUID.randomUUID().toString();
        log.info("Generating KeYmaeraX output content with the unique ID: {}.", uniqueID);
        log.debug("Type name for the header is '{}''.", typeName);

        outputBuilder.append("ArchiveEntry \"Converted ").append(typeName).append(" to KeYmaeraX format with Id as ")
                .append(uniqueID).append("\"\n")
                .append("\tDescription \"Converted ").append(typeName).append(" to KeYmaeraX format with Id as ")
                .append(uniqueID).append("\".\n")
                .append(" \tTitle \"Converted ").append(typeName).append(" to KeYmaeraX format with Id as ")
                .append(uniqueID).append("\".\n")
                .append("\n");
        log.info("Successfully appended file header for KeYmaeraX output content.");
    }

    private void appendProgramVariable(StringBuilder outputBuilder, Set<String> identifiersSet) {
        outputBuilder.append("ProgramVariables\n");
        // Append program variables from identifiersSet
        if (identifiersSet != null && !identifiersSet.isEmpty()) {
            for (String identifier : identifiersSet) {
                if (identifier != null && !identifier.trim().isEmpty())
                    outputBuilder.append("\tReal ").append(identifier).append(";\n");
                else
                    log.warn("Skipping null or empty identifier found in the identifiersSet.");
            }
        } else
            log.debug("No identifiers provided for the ProgramVariables section.");
        outputBuilder.append("End.\n")
                .append("\n");
        log.info("Successfully appended program variables for KeYmaeraX output content.");
    }

    private void appendProblemSection(StringBuilder outputBuilder, String keYmaeraXOutputContent) {
        outputBuilder.append("Problem\n")
                .append("\t").append(keYmaeraXOutputContent).append("\n")
                .append("End.\n")
                .append("\n");
        log.info("Successfully appended problem section for KeYmaeraX output content.");
    }
}