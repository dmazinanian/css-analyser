package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNodeFinder;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareErrorTree;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.parser.LessLexer;

public class LessPreprocessorNodeFinder extends PreprocessorNodeFinder<StyleSheet, ASTCssNode> {
	
	public LessPreprocessorNodeFinder(StyleSheet forStyleSheet) {
		super(forStyleSheet);
	}

	@Override
	public PreprocessorNode<ASTCssNode> perform(PreprocessorNode<ASTCssNode> root, int start, int length) {
	
		List<? extends ASTCssNode> childs = getAllNodesUnder(root.getRealNode());
		ASTCssNode foundNode = null;
		for (ASTCssNode node : childs) {
			LocationInfo locationInfo = getLocationInfoForLessASTCssNode(node);
			if (locationInfo.getOffset() == start && locationInfo.getLength() == length) {
				foundNode = node; 
				break;
			}
		}
		
		return new LessPreprocessorNode(foundNode);
	}
	
	private List<? extends ASTCssNode> getAllNodesUnder(ASTCssNode root) {
		List<ASTCssNode> toReturn = new ArrayList<>();
		for (ASTCssNode node : root.getChilds()) {
			toReturn.addAll(getAllNodesUnder(node));
		}
		toReturn.add(root);
		return toReturn;
	}

	@Override
	public PreprocessorNode<ASTCssNode> perform(int start, int lentgh) {
		return perform(new LessPreprocessorNode(getRealStyleSheet()), start, lentgh);
	}
	
	public static LocationInfo getLocationInfoForLessASTCssNode(ASTCssNode node) {
		HiddenTokenAwareTree firstChild, lastChild;
		if (node.getUnderlyingStructure().getChildren().size() == 0) {
			firstChild = lastChild = node.getUnderlyingStructure();
		} else {
			firstChild = node.getUnderlyingStructure().getChild(0);
			while (firstChild.getChildCount() > 0) {
				if (firstChild.getChild(0).getGeneralType() == LessLexer.EMPTY_COMBINATOR) {
					if (firstChild.getChildCount() > 1)
						firstChild = firstChild.getChild(1);
					else
						throw new RuntimeException("What To Do?");
				} else {
					firstChild = firstChild.getChild(0);	
				}
			}
			
			lastChild = node.getUnderlyingStructure().getChild(node.getUnderlyingStructure().getChildCount() - 1);
			while (lastChild.getChildCount() > 0) {
				lastChild = lastChild.getChild(lastChild.getChildCount() - 1);
			}
	
		}
		
		int line = node.getSourceLine();
		int column = node.getSourceColumn();
		
		int offset = -1, length = -1;
		
		if (firstChild instanceof HiddenTokenAwareErrorTree || 
				lastChild instanceof HiddenTokenAwareErrorTree) {
			throw new IllegalArgumentException("File has syntax errors, so the location info cannot be retrieved.");
		}
		
		try {
			offset = (int)FieldUtils.readField(firstChild.getToken(), "start", true);
			length = (int)FieldUtils.readField(lastChild.getToken(), "stop", true) - offset + 1;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
				
		LocationInfo toReturn = new LocationInfo(line, column, offset, length);
		return toReturn;
	}

}
