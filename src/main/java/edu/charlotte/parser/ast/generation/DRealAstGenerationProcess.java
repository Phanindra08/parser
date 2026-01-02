package edu.charlotte.parser.ast.generation;

import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.grammars.GenerateAstForDReal;
import edu.charlotte.parser.listeners.ast.DRealAstListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DRealAstGenerationProcess extends AbstractAstGenerationProcess<GenerateAstForDReal, DRealAstListener> {

    public DRealAstGenerationProcess(GenerateAstForDReal generateAstForDReal) {
        super(generateAstForDReal);
        log.debug("DRealAstGenerationProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(DRealAstListener listener) {
        return listener.getAst();
    }
}