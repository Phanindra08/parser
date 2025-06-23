package edu.charlotte.parser.ast_generation;

import edu.charlotte.parser.listeners.ast_listeners.DlAstListener;
import edu.charlotte.parser.nodes.ASTNode;
import edu.charlotte.parser.parser_for_grammars.GenerateASTForDL;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DlAstGenerationProcess extends AbstractAstGenerationProcess<GenerateASTForDL, DlAstListener> {

    public DlAstGenerationProcess(GenerateASTForDL generateASTForDL) {
        super(generateASTForDL);
        log.debug("DlAstGenerationProcess is initialized.");
    }

    @Override
    protected ASTNode getAstRootFromListener(DlAstListener listener) {
        return listener.getAST();
    }
}