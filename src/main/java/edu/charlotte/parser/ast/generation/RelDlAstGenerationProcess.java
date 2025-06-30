package edu.charlotte.parser.ast.generation;

import edu.charlotte.parser.listeners.ast.RelDlAstListener;
import edu.charlotte.parser.ast.nodes.AstNode;
import edu.charlotte.parser.grammars.GenerateAstForRelDl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelDlAstGenerationProcess extends AbstractAstGenerationProcess<GenerateAstForRelDl, RelDlAstListener> {

    public RelDlAstGenerationProcess(GenerateAstForRelDl generateAstForRelDl) {
        super(generateAstForRelDl);
        log.debug("RelDlAstGenerationProcess is initialized.");
    }

    @Override
    protected AstNode getAstRootFromListener(RelDlAstListener listener) {
        return listener.getAst();
    }
}