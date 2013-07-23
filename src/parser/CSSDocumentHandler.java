package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Locator;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

import org.w3c.flute.parser.LexicalUnitImpl;
import org.w3c.flute.parser.selectors.AdjacentSelector;
import org.w3c.flute.parser.selectors.AndConditionImpl;
import org.w3c.flute.parser.selectors.AttributeConditionImpl;
import org.w3c.flute.parser.selectors.BeginHyphenAttributeConditionImpl;
import org.w3c.flute.parser.selectors.CaretCondition;
import org.w3c.flute.parser.selectors.ChildSelectorImpl;
import org.w3c.flute.parser.selectors.ClassConditionImpl;
import org.w3c.flute.parser.selectors.ConditionalSelectorImpl;
import org.w3c.flute.parser.selectors.DescendantSelectorImpl;
import org.w3c.flute.parser.selectors.DirectAdjacentSelectorImpl;
import org.w3c.flute.parser.selectors.ElementSelectorImpl;
import org.w3c.flute.parser.selectors.EndsWithCondition;
import org.w3c.flute.parser.selectors.FunctionPseudoClassCondition;
import org.w3c.flute.parser.selectors.IdConditionImpl;
import org.w3c.flute.parser.selectors.LangConditionImpl;
import org.w3c.flute.parser.selectors.NegativeConditionImpl;
import org.w3c.flute.parser.selectors.OneOfAttributeConditionImpl;
import org.w3c.flute.parser.selectors.PseudoClassConditionImpl;
import org.w3c.flute.parser.selectors.PseudoElementSelectorImpl;
import org.w3c.flute.parser.selectors.ContainsCondition;
import org.w3c.flute.parser.selectors.PseudoElementCondition;

import CSSModel.DeclarationValue;
import CSSModel.IndirectAdjacentSelector;
import CSSModel.NamedColors;
import CSSModel.PseudoNegativeClass;
import CSSModel.PseudoElement;
import CSSModel.SelectorConditionType;
import CSSModel.DescendantSelector;
import CSSModel.DirectDescendantSelector;
import CSSModel.ImmediatelyAdjacentSelector;
import CSSModel.AtomicMedia;
import CSSModel.AtomicSelector;
import CSSModel.Declaration;
import CSSModel.AtomicElementSelector;
import CSSModel.GroupedMedia;
import CSSModel.PseudoClass;
import CSSModel.Selector;
import CSSModel.GroupedSelectors;
import CSSModel.SelectorCondition;
import CSSModel.StyleSheet;
import CSSModel.Media;

public class CSSDocumentHandler implements DocumentHandler {

	private Selector currentSelector;
	private Media currentMedia;
	private StyleSheet styleSheet;
	private int numberOfVisitedElements = 0;

	public CSSDocumentHandler(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public void startDocument(InputSource arg0) throws CSSException {
	}

	@Override
	public void endDocument(InputSource arg0) throws CSSException {
	}

	@Override
	public void comment(String arg0) throws CSSException {
	}

	@Override
	public void startFontFace() throws CSSException {
	}

	@Override
	public void endFontFace() throws CSSException {
	}

	@Override
	public void startPage(String arg0, String arg1) throws CSSException {
	}

	@Override
	public void endPage(String arg0, String arg1) throws CSSException {
	}

	@Override
	public void ignorableAtRule(String arg0) throws CSSException {
		// TODO the rules starting with @ which are ignorable
	}

	@Override
	public void importStyle(String arg0, SACMediaList arg1, String arg2)
			throws CSSException {
		//According to CSS3 '@import' rules must occur before all rules other than '@charset' rules
		File file = new File(styleSheet.getFilePath());
		String parentFolder = file.getParent(); 
		CSSParser parser = new CSSParser(parentFolder + "/" + arg0);
		styleSheet.addSelectors(parser.parseAndCreateStyleSheetObject());
	}

	@Override
	public void namespaceDeclaration(String arg0, String arg1)
			throws CSSException {
	}

	@Override
	public void startMedia(SACMediaList arg0) throws CSSException {
		if (currentMedia == null) {
			currentMedia = getMedia(arg0);
		} else {
			GroupedMedia groupedMedia;
			if (currentMedia instanceof AtomicMedia) {
				groupedMedia = new GroupedMedia();
				groupedMedia.addMedia((AtomicMedia) currentMedia);
			} else {
				groupedMedia = (GroupedMedia)currentMedia;
			}
			Media tempMedia = getMedia(arg0);
			if (tempMedia instanceof AtomicMedia)
				groupedMedia.addMedia((AtomicMedia)tempMedia);
			else 
				groupedMedia.addAllMedia((GroupedMedia)tempMedia);
			currentMedia = groupedMedia;
		}	
	}
	
	private Media getMedia(SACMediaList arg0) {
		Media media;
		if (arg0.getLength() == 1) {
			media = new AtomicMedia(arg0.item(0));
		} else {
			GroupedMedia groupedMedia = new GroupedMedia();
			for (int i = 0; i < arg0.getLength(); i++)
				groupedMedia.addMedia(new AtomicMedia(arg0.item(i)));
			media = groupedMedia;
		}
		return media;
	}

	@Override
	public void endMedia(SACMediaList arg0) throws CSSException {
		if (currentMedia instanceof AtomicMedia) {
			currentMedia = null;
		} else {
			GroupedMedia groupedMedia = (GroupedMedia)currentMedia;
			for (int i = 0; i < arg0.getLength(); i++) {
				groupedMedia.removeMedia(arg0.item(i));
			}
			if (groupedMedia.size() == 1)
				currentMedia = new AtomicMedia(groupedMedia.getAtomicMedia(0).getMediaName());
			else if (groupedMedia.size() == 0)
				currentMedia = null;
		}
	}

	@Override
	public void startSelector(SelectorList arg0) throws CSSException {
		startSelector(arg0, null);
	}

	public void startSelector(SelectorList arg0, Locator loc) {
		numberOfVisitedElements++;

		currentSelector = getSelector(arg0, loc);

		if (currentSelector != null)
			styleSheet.addSelector(currentSelector);

		// System.out.println(currentSelector);
	}

	@Override
	public void endSelector(SelectorList arg0) throws CSSException {
		currentSelector = null;
	}

	@Override
	public void property(String arg0, LexicalUnit arg1, boolean arg2)
			throws CSSException {

		// property(arg0, arg1, arg2, null);
		throw new RuntimeException("No locator provided");
	}

	public void property(String arg0, LexicalUnit arg1, boolean arg2,
			Locator locator) throws CSSException {

		List<DeclarationValue> valuesList = getAllValues(arg1);
		
		/*String[] shorthandProperties = new String[] {
					"background", 
					"font",
					"list-style",
					"margin",
					"padding",
					"border",
					"border-color",
					"border-style",
					"border-width," +
					"border-image" +
					"border-top", "border-right", "border-bottom", "border-left" }; */

		Declaration newDeclaration = new Declaration(arg0, valuesList,
				currentSelector, locator.getLineNumber(),
				locator.getColumnNumber(), arg2);

		if (currentSelector != null)
			currentSelector.addCSSRule(newDeclaration);

	}

	// LexicalUnit is a linked list of values for a css property
	private List<DeclarationValue> getAllValues(LexicalUnit value) {

		List<DeclarationValue> accumulator = new ArrayList<>();

		if (value != null) {
			do {
				accumulator.add(new DeclarationValue(getValue(value), value
						.getLexicalUnitType()));
				value = value.getNextLexicalUnit();
			} while (value != null);
		}
		
		return accumulator;
	}

	private String getValue(LexicalUnit value) {
		if (value == null)
			return "";
		switch (value.getLexicalUnitType()) {
		case LexicalUnit.SAC_ATTR:
			return "attr(" + value.getStringValue() + ")";
		case LexicalUnit.SAC_IDENT:
			String stringValue = value.getStringValue();
			String colorEquivalent = getColorEquivalent(stringValue);
			if (colorEquivalent != null)
				stringValue = colorEquivalent;
			return stringValue;
		case LexicalUnit.SAC_STRING_VALUE:
			return "'" + value.getStringValue() + "'";
		case LexicalUnit.SAC_RGBCOLOR:
			// flute models the commas as operators so no separator needed
			return colorValueFromRGB(value.getParameters());
		case LexicalUnitImpl.RGBA_COLOR:
			return colorRGBA(value.getParameters());
		case LexicalUnitImpl.HSL_COLOR:
		case LexicalUnitImpl.HSLA_COLOR:
			return colorFromHSLA(value.getParameters());
		case LexicalUnit.SAC_INTEGER:
			return String.valueOf(value.getIntegerValue());
		case LexicalUnit.SAC_REAL:
			return String.valueOf(value.getFloatValue());
		case LexicalUnit.SAC_CENTIMETER:
		case LexicalUnit.SAC_DEGREE:
		case LexicalUnit.SAC_DIMENSION:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_GRADIAN:
		case LexicalUnit.SAC_HERTZ:
		case LexicalUnit.SAC_KILOHERTZ:
		case LexicalUnit.SAC_MILLIMETER:
		case LexicalUnit.SAC_MILLISECOND:
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_RADIAN:
		case LexicalUnit.SAC_SECOND:
			return String.valueOf(value.getFloatValue()
					+ value.getDimensionUnitText());
		case LexicalUnit.SAC_URI:
			return "url('" + value.getStringValue() + "')";
		case LexicalUnit.SAC_OPERATOR_COMMA:
			return ",";
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
		case LexicalUnit.SAC_FUNCTION:
			return value.getFunctionName() + "("
					+ addSeparators(getAllValues(value.getParameters()), " ")
					+ ")";
		case LexicalUnit.SAC_INHERIT:
			return "inherit";
		case LexicalUnit.SAC_OPERATOR_EXP:
			return "^";
		case LexicalUnit.SAC_OPERATOR_GE:
			return ">=";
		case LexicalUnit.SAC_OPERATOR_GT:
			return (">");
		case LexicalUnit.SAC_OPERATOR_LE:
			return ("<=");
		case LexicalUnit.SAC_OPERATOR_LT:
			return ("<");
		case LexicalUnit.SAC_OPERATOR_MINUS:
			return ("-");
		case LexicalUnit.SAC_OPERATOR_MOD:
			return ("%");
		case LexicalUnit.SAC_OPERATOR_MULTIPLY:
			return ("*");
		case LexicalUnit.SAC_OPERATOR_PLUS:
			return ("+");
		case LexicalUnit.SAC_OPERATOR_SLASH:
			return ("/");
		case LexicalUnit.SAC_OPERATOR_TILDE:
			return ("~");
		case LexicalUnit.SAC_RECT_FUNCTION: {
			// Just return this as a String
			return "rect("
					+ addSeparators(getAllValues(value.getParameters()), " ")
					+ ")";
		}
		case LexicalUnit.SAC_SUB_EXPRESSION:
			// ?
		case LexicalUnit.SAC_UNICODERANGE:
			// Cannot be expressed in CSS2
		case LexicalUnitImpl.PARAN:
			return "(" + addSeparators(getAllValues(value.getParameters()), " ") + ")" ;
		}
		throw new RuntimeException("Unhandled LexicalUnit type "
				+ value.getLexicalUnitType());
	}

	private String addSeparators(List<DeclarationValue> listOfStrings,
			String separator) {
		String toReturn = "";
		for (DeclarationValue dv : listOfStrings)
			toReturn += dv + separator;
		return toReturn.substring(0, toReturn.length() - separator.length());
	}

	private String getColorEquivalent(String stringValue) {

		return NamedColors.getRGBAColorCSSString(stringValue.toLowerCase());

	}

	private Selector getSelector(SelectorList l, Locator loc) {
		Selector s = null;
		if (l.getLength() > 1) {
			GroupedSelectors groupedSelectors = new GroupedSelectors(
					loc.getLineNumber(), loc.getColumnNumber());
			for (int i = 0; i < l.getLength(); i++) {
				try {
					AtomicSelector newAtomicSelector = SACSelectorToAtomicSelector(l
							.item(i));
					newAtomicSelector.setLineNumber(loc.getLineNumber());
					newAtomicSelector.setColumnNumber(loc.getColumnNumber());

					if (currentMedia != null)
						newAtomicSelector.setMedia(currentMedia);
					groupedSelectors.add(newAtomicSelector);
				} catch (Exception ex) {
					// TODO: logger.severe
					System.out.println(ex);
				}
			}
			s = groupedSelectors;
		} else {
			try {
				s = SACSelectorToAtomicSelector(l.item(0));
				s.setLineNumber(loc.getLineNumber());
				s.setColumnNumber(loc.getColumnNumber());
				if (currentMedia != null)
					s.setMedia(currentMedia);
				// styleSheet.addSelector(currentSelector);
			} catch (Exception ex) {
				// TODO: logger.severe..
				System.out.println(ex);
			}
		}
		return s;
	}

	// Adapted from GWT
	// http://google-web-toolkit.googlecode.com/svn-history/r7441/trunk/user/src/com/google/gwt/resources/css/GenerateCssAst.java
	private AtomicSelector SACSelectorToAtomicSelector(
			org.w3c.css.sac.Selector selector) throws Exception {
		// if (selector instanceof CharacterDataSelector) {
		// Unimplemented in flute?
		// }
		if (selector instanceof ElementSelectorImpl) {

			ElementSelectorImpl sacElementSelector = (ElementSelectorImpl) selector;
			AtomicElementSelector atomicElementSelector = new AtomicElementSelector();
			String elementName;
			if (sacElementSelector.getLocalName() == null) {
				elementName = "*";
			} else {
				elementName = sacElementSelector.getLocalName();
			}
			atomicElementSelector.setSelectedElementName(elementName);

			return atomicElementSelector;

		} else if (selector instanceof DescendantSelectorImpl) {

			DescendantSelectorImpl sacDescendantSelector = (DescendantSelectorImpl) selector;
			AtomicSelector parentAtomicSelector = SACSelectorToAtomicSelector(sacDescendantSelector
					.getAncestorSelector());
			AtomicSelector childAtomicSelector = SACSelectorToAtomicSelector(sacDescendantSelector
					.getSimpleSelector());
			DescendantSelector s = new DescendantSelector(parentAtomicSelector,
					childAtomicSelector);

			return s;

		} else if (selector instanceof ChildSelectorImpl) {
			/*
			 * In fact we have three different occasions wherein this happens: A
			 * > B :first-letter :first-line
			 */

			ChildSelectorImpl sacChildSelectorImpl = (ChildSelectorImpl) selector;

			AtomicSelector parentAtomicSelector = SACSelectorToAtomicSelector(sacChildSelectorImpl
					.getAncestorSelector());
			AtomicSelector childAtomicSelector = SACSelectorToAtomicSelector(sacChildSelectorImpl
					.getSimpleSelector());

			AtomicSelector selectorToReturn;

			if (sacChildSelectorImpl.getSimpleSelector() instanceof PseudoElementSelectorImpl) {
				selectorToReturn = parentAtomicSelector;
				PseudoElementSelectorImpl pseudoClass = (PseudoElementSelectorImpl) sacChildSelectorImpl
						.getSimpleSelector();

				((AtomicElementSelector) selectorToReturn)
						.addPseudoClass(new PseudoClass(pseudoClass
								.getLocalName()));

			} else {
				selectorToReturn = new DirectDescendantSelector(
						parentAtomicSelector, childAtomicSelector);
			}

			return selectorToReturn;

		} else if (selector instanceof PseudoElementSelectorImpl) {
			PseudoElementSelectorImpl pseudoElementSelector = (PseudoElementSelectorImpl) selector;
			AtomicElementSelector atomicElementSelector = new AtomicElementSelector();
			atomicElementSelector.addPseudoClass(new PseudoClass(
					pseudoElementSelector.getLocalName()));
			return atomicElementSelector;
		} else if (selector instanceof DirectAdjacentSelectorImpl) {

			DirectAdjacentSelectorImpl sacDirectAdjacentSelector = (DirectAdjacentSelectorImpl) selector;
			AtomicSelector parentSelector = SACSelectorToAtomicSelector(sacDirectAdjacentSelector
					.getSelector());
			AtomicSelector childSelector = SACSelectorToAtomicSelector(sacDirectAdjacentSelector
					.getSiblingSelector());
			return new ImmediatelyAdjacentSelector(parentSelector,
					childSelector);
		} else if (selector instanceof AdjacentSelector) {
			AdjacentSelector sacAdjacentSelector = (AdjacentSelector) selector;
			AtomicSelector parentSelector = SACSelectorToAtomicSelector(sacAdjacentSelector
					.getSelector());
			AtomicSelector childSelector = SACSelectorToAtomicSelector(sacAdjacentSelector
					.getSiblingSelector());
			return new IndirectAdjacentSelector(parentSelector, childSelector);
		} else if (selector instanceof ConditionalSelectorImpl) {

			ConditionalSelector sacConditionalSelector = (ConditionalSelectorImpl) selector;
			AtomicElementSelector atomicElementSelector = (AtomicElementSelector) SACSelectorToAtomicSelector(sacConditionalSelector
					.getSimpleSelector());
			getConditions(sacConditionalSelector.getCondition(),
					atomicElementSelector);
			return atomicElementSelector;
		} else {
			throw new Exception("Selector not supported: " + selector);
		}
	}

	private void getConditions(Condition sacCondition,
			AtomicElementSelector atomicElementSelector) throws Exception {
		if (sacCondition == null)
			return;
		if (sacCondition instanceof AndConditionImpl) {

			AndConditionImpl andCondition = (AndConditionImpl) sacCondition;
			getConditions(andCondition.getFirstCondition(),
					atomicElementSelector);
			getConditions(andCondition.getSecondCondition(),
					atomicElementSelector);

		} else if (sacCondition instanceof ClassConditionImpl) {

			ClassConditionImpl classCond = (ClassConditionImpl) sacCondition;
			atomicElementSelector.addClassName(classCond.getValue());
		} else if (sacCondition instanceof PseudoClassConditionImpl) {

			PseudoClassConditionImpl pseudoCond = (PseudoClassConditionImpl) sacCondition;
			atomicElementSelector.addPseudoClass(new PseudoClass(pseudoCond
					.getValue()));

		} else if (sacCondition instanceof IdConditionImpl) {

			IdConditionImpl c = (IdConditionImpl) sacCondition;
			atomicElementSelector.setIDName(c.getValue());

		} else if (sacCondition instanceof LangConditionImpl) {

			LangConditionImpl langCondition = (LangConditionImpl) sacCondition;
			atomicElementSelector.addPseudoClass(new PseudoClass("lang",
					langCondition.getLang()));

		} else if (sacCondition instanceof AttributeConditionImpl) {

			AttributeConditionImpl attributeConditionImpl = (AttributeConditionImpl) sacCondition;
			SelectorCondition selectorCondition = new SelectorCondition(
					attributeConditionImpl.getLocalName());
			String value = attributeConditionImpl.getValue();
			if (value != null) {
				selectorCondition
						.setConditionType(SelectorConditionType.VALUE_EQUALS_EXACTLY);
				selectorCondition.setConditionValue(attributeConditionImpl
						.getValue());
			}
			atomicElementSelector.addCondition(selectorCondition);

		} else if (sacCondition instanceof OneOfAttributeConditionImpl) {

			OneOfAttributeConditionImpl oneOfAttrCondition = (OneOfAttributeConditionImpl) sacCondition;
			SelectorCondition selectorCondition = new SelectorCondition(
					oneOfAttrCondition.getLocalName(),
					oneOfAttrCondition.getValue(),
					SelectorConditionType.VALUE_CONTAINS_WORD_SPACE_SEPARATED);
			atomicElementSelector.addCondition(selectorCondition);

		} else if (sacCondition instanceof BeginHyphenAttributeConditionImpl) {

			BeginHyphenAttributeConditionImpl oneOfAttrCondition = (BeginHyphenAttributeConditionImpl) sacCondition;
			SelectorCondition selectorCondition = new SelectorCondition(
					oneOfAttrCondition.getLocalName(),
					oneOfAttrCondition.getValue(),
					SelectorConditionType.VALUE_START_WITH_DASH_SEPARATED);
			atomicElementSelector.addCondition(selectorCondition);

		} else if (sacCondition instanceof CaretCondition) {

			CaretCondition oneOfAttrCondition = (CaretCondition) sacCondition;
			SelectorCondition selectorCondition = new SelectorCondition(
					oneOfAttrCondition.getLocalName(),
					oneOfAttrCondition.getValue(),
					SelectorConditionType.VALUE_STARTS_WITH);
			atomicElementSelector.addCondition(selectorCondition);

		} else if (sacCondition instanceof ContainsCondition) {

			ContainsCondition oneOfAttrCondition = (ContainsCondition) sacCondition;
			SelectorCondition selectorCondition = new SelectorCondition(
					oneOfAttrCondition.getLocalName(),
					oneOfAttrCondition.getValue(),
					SelectorConditionType.VALUE_CONTAINS);
			atomicElementSelector.addCondition(selectorCondition);

		} else if (sacCondition instanceof EndsWithCondition) {

			EndsWithCondition oneOfAttrCondition = (EndsWithCondition) sacCondition;
			SelectorCondition selectorCondition = new SelectorCondition(
					oneOfAttrCondition.getLocalName(),
					oneOfAttrCondition.getValue(),
					SelectorConditionType.VALUE_ENDS_WITH);
			atomicElementSelector.addCondition(selectorCondition);

		} else if (sacCondition instanceof NegativeConditionImpl) {

			NegativeConditionImpl condition = (NegativeConditionImpl) sacCondition;
			SelectorList l = condition.getSelectorList();
			Locator loc = condition.getLocator();
			Selector s = getSelector(l, loc);

			atomicElementSelector.addPseudoClass(new PseudoNegativeClass("not",
					s));

		} else if (sacCondition instanceof FunctionPseudoClassCondition) {

			FunctionPseudoClassCondition pcs = (FunctionPseudoClassCondition) sacCondition;
			atomicElementSelector.addPseudoClass(new PseudoClass(pcs
					.getLocalName(), pcs.getValue()));

		} else if (sacCondition instanceof PseudoElementCondition) {

			PseudoElementCondition spcc = (PseudoElementCondition) sacCondition;
			atomicElementSelector.addPseudoElement(new PseudoElement(spcc
					.getName()));

		} else {
			throw new Exception("Condition not supported: " + sacCondition);
		}
	}

	public int getNumberOfVisitedSelectors() {
		return numberOfVisitedElements;
	}

	private int getRgbComponentValue(LexicalUnit color) {
		switch (color.getLexicalUnitType()) {
		case LexicalUnit.SAC_INTEGER:
			return Math.min(color.getIntegerValue(), 255);
		case LexicalUnit.SAC_PERCENTAGE:
			return (int) Math.min(color.getFloatValue() * 255, 255);
		default:
			throw new CSSException(CSSException.SAC_SYNTAX_ERR,
					"RGB component value must be integer or percentage, was "
							+ color, null);
		}
	}

	private String colorValueFromRGB(LexicalUnit colors) {
		LexicalUnit red = colors;
		int r = getRgbComponentValue(red);
		LexicalUnit green = red.getNextLexicalUnit().getNextLexicalUnit();
		int g = getRgbComponentValue(green);
		LexicalUnit blue = green.getNextLexicalUnit().getNextLexicalUnit();
		int b = getRgbComponentValue(blue);
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, 1F);
	}

	private String colorRGBA(LexicalUnit colors) {
		LexicalUnit red = colors;
		int r = getRgbComponentValue(red);
		LexicalUnit green = red.getNextLexicalUnit().getNextLexicalUnit();
		int g = getRgbComponentValue(green);
		LexicalUnit blue = green.getNextLexicalUnit().getNextLexicalUnit();
		int b = getRgbComponentValue(blue);
		LexicalUnit alpha = blue.getNextLexicalUnit().getNextLexicalUnit();
		// The problem is, the value is either in Integer or float so we need to
		// check for both of them
		float a = Math.min(alpha.getIntegerValue(), 1);
		if (a == 0) // Lets try float
			a = Math.min(alpha.getFloatValue(), 1);
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, a);
	}

	private String colorFromHSLA(LexicalUnit value) {

		LexicalUnit hue = value;
		float h = Math.min(hue.getIntegerValue(), 360) / 360F;
		LexicalUnit saturation = hue.getNextLexicalUnit().getNextLexicalUnit();
		float s = Math.min(saturation.getFloatValue(), 100) / 100F;
		LexicalUnit lightness = saturation.getNextLexicalUnit()
				.getNextLexicalUnit();
		float l = Math.min(lightness.getFloatValue(), 100) / 100F;
		float a = 1F;
		if (lightness.getNextLexicalUnit() != null) {
			LexicalUnit alpha = lightness.getNextLexicalUnit()
					.getNextLexicalUnit();
			// Same as colorRGBA
			a = Math.min(alpha.getIntegerValue(), 1);
			if (a == 0)
				a = Math.min(alpha.getFloatValue(), 1);
		}

		int r, g, b;
		float m2;
		if (l <= 0.5)
			m2 = l * (s + 1);
		else
			m2 = l + s - l * s;

		float m1 = l * 2 - m2;
		r = (int) (hue_to_rgb(m1, m2, h + 1 / 3F) * 255);
		g = (int) (hue_to_rgb(m1, m2, h) * 255);
		b = (int) (hue_to_rgb(m1, m2, h - 1 / 3F) * 255);

		return String.format("rgba(%s, %s, %s, %s)", r, g, b, a);

	}

	private float hue_to_rgb(float m1, float m2, float h) {
		if (h < 0)
			h++;
		if (h > 1)
			h--;
		if (h * 6 < 1)
			return m1 + (m2 - m1) * h * 6;
		if (h * 2 < 1)
			return m2;
		if (h * 3 < 2)
			return m1 + (m2 - m1) * (2 / 3F - h) * 6;
		return m1;
	}

}