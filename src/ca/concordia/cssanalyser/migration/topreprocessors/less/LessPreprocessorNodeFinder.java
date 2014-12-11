package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNode;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorNodeFinder;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.parser.LessLexer;

public class LessPreprocessorNodeFinder extends PreprocessorNodeFinder<ASTCssNode> {
	
	private final StyleSheet lessStyleSheet;
	
	public LessPreprocessorNodeFinder(StyleSheet forStyleSheet) {
		this.lessStyleSheet = forStyleSheet;
	}

	@Override
	public PreprocessorNode<ASTCssNode> perform(PreprocessorNode<ASTCssNode> root, int start, int length) {
		
		ASTCssNode toReturn = null;
		
		List<ASTCssNode> childs = lessStyleSheet.getChilds();
		
		lessStyleSheet.getParent();
		
		//LocationInfo locationInfo = getLocationInfoForLessASTCssNode(node)
		
		return new LessPreprocessorNode(toReturn);
	}
	
	public static LocationInfo getLocationInfoForLessASTCssNode(ASTCssNode node) {
		HiddenTokenAwareTree firstChild, lastChild;
		if (node.getUnderlyingStructure().getChildren().size() == 0) {
			firstChild = lastChild = node.getUnderlyingStructure();
		} else {
			firstChild = node.getUnderlyingStructure().getChild(0);
			while (firstChild.getChildCount() > 0) {
				if (firstChild.getChild(0).getType() == LessLexer.EMPTY_COMBINATOR) {
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
