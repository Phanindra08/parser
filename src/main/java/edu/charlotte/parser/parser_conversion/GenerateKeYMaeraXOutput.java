package edu.charlotte.parser.parser_conversion;

import org.springframework.context.annotation.Configuration;
import java.util.Set;
import java.util.UUID;

@Configuration
public class GenerateKeYMaeraXOutput {
    public String createFileContent(Set<String> identifiersSet, String keYMaeraXOutput) {
        String uniqueID = UUID.randomUUID().toString();
        StringBuilder keYMaeraXOutputContent = new StringBuilder();
        keYMaeraXOutputContent.append("ArchiveEntry \"Converted Dynamic Differential Logic to KeYMaeraX format with Id as ")
                .append(uniqueID).append("\"\n")
                .append("\tDescription \"Converted Dynamic Differential Logic to KeYMaeraX format with Id as ")
                .append(uniqueID).append("\".\n")
                .append(" \tTitle \"Converted Dynamic Differential Logic to KeYMaeraX format with Id as ")
                .append(uniqueID).append("\".\n")
                .append("\n")
                .append("ProgramVariables\n");

        // Append program variables from identifiersSet
        for (String identifier : identifiersSet)
            keYMaeraXOutputContent.append("\tReal ").append(identifier).append(";\n");

        keYMaeraXOutputContent.append("End.\n")
                .append("\n")
                .append("Problem\n")
                .append("\t").append(keYMaeraXOutput).append("\n")
                .append("End.\n")
                .append("\n")
                .append("End.");

        return keYMaeraXOutputContent.toString();
    }
}
