package edu.charlotte.parser.ast.generation;

import edu.charlotte.parser.listeners.ast.DlAstListener;
import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.grammars.GenerateAstForDl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DlAstGenerationProcess extends AbstractAstGenerationProcess<GenerateAstForDl, DlAstListener> {

    public DlAstGenerationProcess(GenerateAstForDl generateAstForDl) {
        super(generateAstForDl);
        log.debug("DlAstGenerationProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(DlAstListener listener) {
        return listener.getAst();
    }
}