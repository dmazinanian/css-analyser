package ca.concordia.cssanalyser.migration.topreprocessors.less;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;

public class LessPreprocessorNode extends PreprocessorNode<ASTCssNode> {

	public LessPreprocessorNode(ASTCssNode realPreprocessorNode) {
		super(realPreprocessorNode);
	}
	
}
