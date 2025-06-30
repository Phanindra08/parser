package edu.charlotte.parser.conversions.reldl.dl;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.utils.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class RelDlToDlConverter {
    private static final String REL_DL_GRAMMAR_OPERATORS_SYMBOL = "#";
    private static final Map<String, String> REL_DL_TO_DL_VALUES_MAPPING = new HashMap<>();

    private final Map<String, String> rightTermIdentifiersConversionMap;
    private boolean isRightTermIdentifier = false;
    @Getter
    private final Set<String> identifiers;

    static {
        REL_DL_TO_DL_VALUES_MAPPING.put(Constants.REL_DL_COMMA, ";");
        REL_DL_TO_DL_VALUES_MAPPING.put(Constants.REL_DL_OPEN_BRACKETS, "");
        REL_DL_TO_DL_VALUES_MAPPING.put(Constants.REL_DL_CLOSE_BRACKETS, "");
        REL_DL_TO_DL_VALUES_MAPPING.put(Constants.LEFT_PROGRAM, "");
        REL_DL_TO_DL_VALUES_MAPPING.put(Constants.RIGHT_PROGRAM, "");
        log.info("RelDlToDlConverter static mapping initialized with {} entries.", REL_DL_TO_DL_VALUES_MAPPING.size());
    }

    public RelDlToDlConverter(Map<Character, Set<String>> categorizedIdentifiers) {
        Objects.requireNonNull(categorizedIdentifiers, "Categorized identifiers map cannot be null.");
        this.rightTermIdentifiersConversionMap = new HashMap<>();
        this.identifiers = new HashSet<>();
        this.initializingRightTermIdentifiersConversionMap(categorizedIdentifiers);
        log.info("RelDlToDlConverter instance is created.");
    }

    private void initializingRightTermIdentifiersConversionMap(Map<Character, Set<String>> identifiers) {
        log.debug("Started to identify and map right term identifiers for conversion.");
        final Set<String> rightTermIdentifiers = new HashSet<>();
        final Set<String> remainingIdentifiers = new HashSet<>();
        identifiers.forEach((key, value) -> {
            if(value == null)
                log.warn("Null set found for key '{}', skipping.", key);
            else {
                log.debug("Identifiers for the '{}' term are: {}", key, value);
                if (key == Constants.PROGRAM_CONSIDERED_R)
                    rightTermIdentifiers.addAll(value);
                else
                    remainingIdentifiers.addAll(value);
            }
        });

        for (String identifier : rightTermIdentifiers) {
            int counter = 1;
            String newIdentifier;
            do {
                newIdentifier = identifier + counter;
                if (!remainingIdentifiers.contains(newIdentifier) && !this.rightTermIdentifiersConversionMap.containsValue(newIdentifier)) {
                    this.rightTermIdentifiersConversionMap.put(identifier, newIdentifier);
                    log.debug("Mapped right term identifier '{}' to unique identifier '{}'.", identifier, newIdentifier);
                    break;
                }
                counter++;
            } while (true);
        }
        this.setAllIdentifiers(remainingIdentifiers, this.rightTermIdentifiersConversionMap.values());
    }

    private void setAllIdentifiers(Set<String> remainingIdentifiers, Collection<String> convertedRightTermIdentifiers) {
        this.identifiers.addAll(remainingIdentifiers);
        this.identifiers.addAll(convertedRightTermIdentifiers);
        log.debug("Collected all identifiers used in '{}' converted from '{}'. Total: {}",
                Constants.DIFFERENTIAL_DYNAMIC_LOGIC, Constants.RELATIONAL_DYNAMIC_LOGIC, this.identifiers.size());
    }

    public void convertToDlAst(AstNode node) {
        if (node == null) {
            log.debug("Encountered Null Node during AST traversal, skipping conversion.");
            return;
        }

        if (node.getValue() == null) {
            log.warn("Node value is null, skipping conversion for this node.");
        } else {
            if (node.getValue().equals("Relational Term") && !node.getChildren().isEmpty() &&
                    node.getChildren().getLast().getValue().equals(Constants.RIGHT_PROGRAM)) {
                this.isRightTermIdentifier = true;
                log.debug("Node value '{}' is a right term, setting isRightTermIdentifier to true.", node.getValue());
            } else if (node.getValue().equals(",#")) {
                this.isRightTermIdentifier = true;
                log.debug("Node value '{}' indicates the start of the right program, setting isRightTermIdentifier to true.", node.getValue());
            } else if (node.getValue().equals(Constants.RIGHT_PROGRAM) || node.getValue().equals(")#")) {
                this.isRightTermIdentifier = false;
                log.debug("Node value '{}' indicates the end of the right program or term, setting isRightTermIdentifier to false.", node.getValue());
            }

            if (REL_DL_TO_DL_VALUES_MAPPING.containsKey(node.getValue())) {
                log.debug("Mapping Rel DL value: '{}' to '{}'.", node.getValue(), REL_DL_TO_DL_VALUES_MAPPING.get(node.getValue()));
                node.setValue(REL_DL_TO_DL_VALUES_MAPPING.get(node.getValue()));
            } else if (node.getValue().contains(REL_DL_GRAMMAR_OPERATORS_SYMBOL)) {
                log.debug("Removing '{}' from the node value: '{}'.", REL_DL_GRAMMAR_OPERATORS_SYMBOL, node.getValue());
                node.setValue(node.getValue().replace(REL_DL_GRAMMAR_OPERATORS_SYMBOL, ""));
            }

            if (this.isRightTermIdentifier) {
                String originalValue = node.getValue();
                if (this.rightTermIdentifiersConversionMap.containsKey(originalValue)) {
                    log.debug("Converting the right term identifier: '{}' to '{}'.", node.getValue(), this.rightTermIdentifiersConversionMap.get(node.getValue()));
                    node.setValue(this.rightTermIdentifiersConversionMap.get(node.getValue()));
                } else if(originalValue.endsWith("'") && this.rightTermIdentifiersConversionMap.containsKey(originalValue.substring(0, originalValue.length() - 1))) {
                    String convertedValue = this.rightTermIdentifiersConversionMap.get(originalValue.substring(0, originalValue.length() - 1)) + "'";
                    log.debug("Converting the right term identifier prime: '{}' to '{}'.", originalValue, convertedValue);
                    node.setValue(convertedValue);
                }
            }
        }

        for (AstNode childNode : node.getChildren())
            this.convertToDlAst(childNode);
    }

    public void convertRelDlToDl(AstNode astRoot) {
        Objects.requireNonNull(astRoot, "Ast root node cannot be null for conversion.");
        log.info("Starting conversion of AST from Rel DL to DL format.");

        this.isRightTermIdentifier = false;
        convertToDlAst(astRoot);
        log.debug("AST conversion from Relational DL to Dynamic Logic format completed.");
    }
}