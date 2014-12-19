package ca.concordia.cssanalyser.migration.topreprocessors.less;

import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.RuleSet;

public class LessPreprocessorNode extends PreprocessorNode<ASTCssNode> {

	public LessPreprocessorNode(ASTCssNode realPreprocessorNode) {
		super(realPreprocessorNode);
	}

	@Override
	public void deleteChild(PreprocessorNode<ASTCssNode> child) {
		Body bodyToAdd = getBodyOfCurrentNode();
		bodyToAdd.removeMember(child.getRealNode());
	}

	@Override
	public void addChild(PreprocessorNode<ASTCssNode> child) {
		Body body = getBodyOfCurrentNode();
		body.addMember(child.getRealNode());
	}

	@Override
	public PreprocessorNode<ASTCssNode> getParent() {
		return new LessPreprocessorNode(getRealNode().getParent());
	}
	
	private Body getBodyOfCurrentNode() {
		Body bodyToAdd = null;
		if (getRealNode() instanceof Body) {
			bodyToAdd = (Body)getRealNode();
		} else if (getRealNode() instanceof RuleSet) {
			bodyToAdd = ((RuleSet)getRealNode()).getBody();
		} else {
			throw new RuntimeException("Node is not an instance of Body");
		}
		return bodyToAdd;
	}
	
}
