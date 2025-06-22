package edu.charlotte.parser.parser_conversion;

import edu.charlotte.parser.listeners.RelDlAstListener;
import edu.charlotte.parser.nodes.ASTNode;
import edu.charlotte.parser.parser_for_grammars.GenerateASTForRelDL;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelDLAstGenerationProcess extends AbstractAstGenerationProcess<GenerateASTForRelDL, RelDlAstListener> {

    public RelDLAstGenerationProcess(GenerateASTForRelDL generateASTForRelDL) {
        super(generateASTForRelDL);
        log.debug("RelDLAstGenerationProcess is initialized.");
    }

    @Override
    protected ASTNode getAstRootFromListener(RelDlAstListener listener) {
        return listener.getAST();
    }
}