package ca.concordia.cssanalyser.parser.flute;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
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
import org.w3c.flute.parser.selectors.ContainsCondition;
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
import org.w3c.flute.parser.selectors.PseudoElementCondition;
import org.w3c.flute.parser.selectors.PseudoElementSelectorImpl;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationEquivalentValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValueFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.AdjacentSiblingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.ChildSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Combinator;
import ca.concordia.cssanalyser.cssmodel.selectors.DescendantSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.NegationPseudoClass;
import ca.concordia.cssanalyser.cssmodel.selectors.PseudoClass;
import ca.concordia.cssanalyser.cssmodel.selectors.PseudoElement;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.cssmodel.selectors.SiblingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.SimpleSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorCondition;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorConditionType;



public class CSSDocumentHandler implements DocumentHandler {
	
	private static final Logger LOGGER = FileLogger.getLogger(CSSDocumentHandler.class);

	private Selector currentSelector;
	private Set<MediaQueryList> currentMediaQueryLists;
	private StyleSheet styleSheet;
	private int numberOfVisitedElements = 0;

	public CSSDocumentHandler(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
		currentMediaQueryLists = new LinkedHashSet<>();
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
		// the rules starting with @ which are ignorable
	}

	@Override
	public void importStyle(String path, SACMediaList forMedia, String arg2)
			throws CSSException {
		
	}
	
	public void importStyle(String path, MediaQueryList forMedia, String arg2) throws CSSException  {
		//According to CSS3 '@import' rules must occur before all rules other than '@charset' rule
			File file = new File(styleSheet.getFilePath());
			String parentFolder = file.getParent(); 
			FluteCSSParser parser = new FluteCSSParser();
			try {
				StyleSheet importedStyleSheet = parser.parseExternalCSS(parentFolder + "/" + path);
				importedStyleSheet.addMediaQueryList(forMedia);
				styleSheet.addSelectors(importedStyleSheet);
			} catch (Exception ex) {
				LOGGER.warn("Couldn't parse or import " + parentFolder + "/" + path);
			}
	}

	@Override
	public void namespaceDeclaration(String arg0, String arg1) throws CSSException {
	}

	@Override
	public void startMedia(SACMediaList mediaList) throws CSSException {
//		if (currentMediaQueryList == null) {
//			currentMediaQueryList = getMedia(mediaList);
//		} else {
//			MediaQueryList groupedMedia;
//			if (currentMediaQueryList instanceof MediaQuery) {
//				groupedMedia = new MediaQueryList();
//				groupedMedia.addMedia((MediaQuery) currentMediaQueryList);
//			} else {
//				groupedMedia = (MediaQueryList)currentMediaQueryList;
//			}
//			Media tempMedia = getMedia(mediaList);
//			if (tempMedia instanceof MediaQuery)
//				groupedMedia.addMedia((MediaQuery)tempMedia);
//			else 
//				groupedMedia.addAllMedia((MediaQueryList)tempMedia);
//			currentMediaQueryList = groupedMedia;
//		}	
	}
	// Change these methods to accept media query list. also the parser
	public void startMedia(MediaQueryList mediaQueryList) {
		currentMediaQueryLists.add(mediaQueryList);
	}
	
	public void endMedia(MediaQueryList mediaQueryList) {
		currentMediaQueryLists.remove(mediaQueryList);
	}
	
//	private Media getMedia(SACMediaList sacMedia) {
//		Media media;
//		if (sacMedia.getLength() == 1) {
//			media = new MediaQuery(sacMedia.item(0));
//		} else {
//			MediaQueryList groupedMedia = new MediaQueryList();
//			for (int i = 0; i < sacMedia.getLength(); i++)
//				groupedMedia.addMedia(new MediaQuery(sacMedia.item(i)));
//			media = groupedMedia;
//		}
//		return media;
//	}

	@Override
	public void endMedia(SACMediaList mediaList) throws CSSException {
//		if (currentMediaQueryList instanceof MediaQuery) {
//			currentMediaQueryList = null;
//		} else {
//			MediaQueryList groupedMedia = (MediaQueryList)currentMediaQueryList;
//			for (int i = 0; i < mediaList.getLength(); i++) {
//				groupedMedia.removeMedia(mediaList.item(i));
//			}
//			if (groupedMedia.size() == 1)
//				currentMediaQueryList = new MediaQuery(groupedMedia.getAtomicMedia(0).getMediaType());
//			else if (groupedMedia.size() == 0)
//				currentMediaQueryList = null;
//		}
	}

	@Override
	public void startSelector(SelectorList selectorList) throws CSSException {
		throw new CSSException("No locator is provided.");
	}

	public void startSelector(SelectorList selectorList, Locator loc) {
		numberOfVisitedElements++;

		currentSelector = getSelector(selectorList, loc);

		if (currentSelector != null)
			styleSheet.addSelector(currentSelector);
	}

	@Override
	public void endSelector(SelectorList selectorList) throws CSSException {
		currentSelector = null;
	}

	@Override
	public void property(String propertyName, LexicalUnit values, boolean isImportant)
			throws CSSException {

		// property(arg0, arg1, arg2, null);
		throw new RuntimeException("No locator provided");
	}

	public void property(String propertyName, LexicalUnit values, boolean isImportant, Locator locator) {
		
		try {
			List<DeclarationValue> valuesList = getAllValues(propertyName, values);
	
			Declaration newDeclaration = null;
	
			newDeclaration = DeclarationFactory.getDeclaration(propertyName, valuesList, currentSelector, locator.getLineNumber(), locator.getColumnNumber(), isImportant, true);
		
			if (currentSelector != null)
				currentSelector.addDeclaration(newDeclaration);
			
		} catch (Exception ex) {
			LOGGER.warn(ex.toString());
		}

	}
	
	/**
	 * Gets a {@link Selector} from a SAC Selector List
	 * @param list a SAC selector list, a list of comma separated SAC selectors
	 * @param locator SAC Locator pointing to the location of the selector list 
	 * @return
	 */
	private Selector getSelector(SelectorList list, Locator locator) {
		Selector s = null;
		if (list.getLength() > 1) {
			GroupingSelector groupedSelectors = new GroupingSelector();
			groupedSelectors.setLineNumber(locator.getLineNumber());
			for (int i = 0; i < list.getLength(); i++) {
				try {
					BaseSelector newAtomicSelector = SACSelectorToAtomicSelector(list.item(i));
					newAtomicSelector.setLineNumber(locator.getLineNumber());
					newAtomicSelector.setColumnNumber(locator.getColumnNumber() - newAtomicSelector.toString().length() + 1);
					if (currentMediaQueryLists.size() > 0)
						newAtomicSelector.addMediaQueryLists(currentMediaQueryLists);
					groupedSelectors.add(newAtomicSelector);
				} catch (Exception ex) {
					LOGGER.warn(ex.toString());
				}
			}
			groupedSelectors.setColumnNumber(locator.getColumnNumber() - groupedSelectors.toString().length() + 1);
			groupedSelectors.addMediaQueryLists(currentMediaQueryLists);
			s = groupedSelectors;
		} else {
			try {
				s = SACSelectorToAtomicSelector(list.item(0));
				s.setLineNumber(locator.getLineNumber());
				s.setColumnNumber(locator.getColumnNumber() - s.toString().length() + 1);
				if (currentMediaQueryLists.size() > 0)
					s.addMediaQueryLists(currentMediaQueryLists);
				// styleSheet.addSelector(currentSelector);
			} catch (Exception ex) {
				LOGGER.warn(ex.toString());
			}
		}
		return s;
	}

	/**
	 * Returns an {@link BaseSelector} from a given SAC selector
	 * Adapted from GWT http://google-web-toolkit.googlecode.com/svn-history/r7441/trunk/user/src/com/google/gwt/resources/css/GenerateCssAst.java
	 * @param selector
	 * @return
	 * @throws Exception
	 */
	private BaseSelector SACSelectorToAtomicSelector(org.w3c.css.sac.Selector selector) throws Exception {
		// if (selector instanceof CharacterDataSelector) {
		// Unimplemented in flute?
		// }
		if (selector instanceof ElementSelectorImpl) {

			ElementSelectorImpl sacElementSelector = (ElementSelectorImpl) selector;
			SimpleSelector atomicElementSelector = new SimpleSelector();
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
			BaseSelector parentAtomicSelector = SACSelectorToAtomicSelector(sacDescendantSelector.getAncestorSelector());
			SimpleSelector childAtomicSelector = (SimpleSelector)SACSelectorToAtomicSelector(sacDescendantSelector.getSimpleSelector());
			DescendantSelector s = new DescendantSelector(parentAtomicSelector, childAtomicSelector);

			return s;

		} else if (selector instanceof ChildSelectorImpl) {
			/*
			 * In fact we have three different occasions wherein this happens: A
			 * > B :first-letter :first-line
			 */

			ChildSelectorImpl sacChildSelectorImpl = (ChildSelectorImpl) selector;

			BaseSelector parentAtomicSelector = SACSelectorToAtomicSelector(sacChildSelectorImpl.getAncestorSelector());
			SimpleSelector childAtomicSelector = (SimpleSelector)SACSelectorToAtomicSelector(sacChildSelectorImpl.getSimpleSelector());

			BaseSelector selectorToReturn;

			if (sacChildSelectorImpl.getSimpleSelector() instanceof PseudoElementSelectorImpl) {
				selectorToReturn = parentAtomicSelector;
				PseudoElementSelectorImpl pseudoClass = (PseudoElementSelectorImpl) sacChildSelectorImpl.getSimpleSelector();
				SimpleSelector simpleSelector;
				if (selectorToReturn instanceof SimpleSelector) // in case of .test:test
					simpleSelector = ((SimpleSelector) selectorToReturn);
				else // in case of .test .test2:test
					simpleSelector = ((Combinator)selectorToReturn).getRightHandSideSelector();
				
				simpleSelector.addPseudoClass(new PseudoClass(pseudoClass.getLocalName()));

			} else {
				selectorToReturn = new ChildSelector(parentAtomicSelector, childAtomicSelector);
			}

			return selectorToReturn;

		} else if (selector instanceof PseudoElementSelectorImpl) {
			
			PseudoElementSelectorImpl pseudoElementSelector = (PseudoElementSelectorImpl) selector;
			SimpleSelector atomicElementSelector = new SimpleSelector();
			atomicElementSelector.addPseudoClass(new PseudoClass(pseudoElementSelector.getLocalName()));
			return atomicElementSelector;
			
		} else if (selector instanceof DirectAdjacentSelectorImpl) {

			DirectAdjacentSelectorImpl sacDirectAdjacentSelector = (DirectAdjacentSelectorImpl) selector;
			BaseSelector parentSelector = SACSelectorToAtomicSelector(sacDirectAdjacentSelector.getSelector());
			SimpleSelector childSelector = (SimpleSelector)SACSelectorToAtomicSelector(sacDirectAdjacentSelector.getSiblingSelector());
			return new AdjacentSiblingSelector(parentSelector, childSelector);
			
		} else if (selector instanceof AdjacentSelector) {
			
			AdjacentSelector sacAdjacentSelector = (AdjacentSelector) selector;
			BaseSelector parentSelector = SACSelectorToAtomicSelector(sacAdjacentSelector.getSelector());
			SimpleSelector childSelector = (SimpleSelector)SACSelectorToAtomicSelector(sacAdjacentSelector.getSiblingSelector());
			return new SiblingSelector(parentSelector, childSelector);
			
		} else if (selector instanceof ConditionalSelectorImpl) {

			ConditionalSelector sacConditionalSelector = (ConditionalSelectorImpl) selector;
			SimpleSelector atomicElementSelector = (SimpleSelector) SACSelectorToAtomicSelector(sacConditionalSelector.getSimpleSelector());
			getConditions(sacConditionalSelector.getCondition(), atomicElementSelector);
			return atomicElementSelector;
			
		} else {
			throw new Exception("Selector not supported: " + selector);
		}
	}

	/**
	 * Adds the conditions (like pseudo classes, etc) to the given selector,
	 * based on the SAC conditions
	 * @param sacCondition
	 * @param atomicElementSelector
	 * @throws Exception
	 */
	private void getConditions(Condition sacCondition, SimpleSelector atomicElementSelector) throws Exception {
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
			atomicElementSelector.setElementID(c.getValue());

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
				selectorCondition.setValue(attributeConditionImpl
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
			// Selector "s" shoule be a simple selector, based on W3C http://www.w3.org/TR/css3-selectors/
			if ((s instanceof SimpleSelector)) {
				atomicElementSelector.addPseudoClass(new NegationPseudoClass((BaseSelector)s));
			} else {
				LOGGER.warn("The parameter of not() pseudo-element should be a simple CSS selector. ");
			}

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

	/**
	 * This method gives a <code>List<ca.concordia.cssanalyser.cssmodel.declaration.DeclarationValue></code>
	 * From a given LexicalUnit value from SAC. <br />
	 * Note that in SAC, LexicalUnit is a linked list of values for a CSS property
	 * @param value
	 */
	private List<DeclarationValue> getAllValues(String propertyName, LexicalUnit value) throws Exception {

		List<DeclarationValue> accumulator = new ArrayList<>();

		if (value != null) {
			do {
				
				DeclarationValue decValue = getValue(propertyName, value);
				if (value != null)
					accumulator.add(decValue);
				value = value.getNextLexicalUnit();
			} while (value != null);
		}
		
		return accumulator;
	}

	/**
	 * This method mainly returns {@link DeclarationValue} or {@link DeclarationEquivalentValue},
	 * based on the given SAC value and SAC property
	 * @param value
	 */
	private DeclarationValue getValue(String propertyName, LexicalUnit value) throws Exception {
		if (value == null)
			return null;
		switch (value.getLexicalUnitType()) {
		case LexicalUnit.SAC_ATTR:
			/* 
			 * attr(). The W3C CSS3 specification says that they may
			 * drop this kind of value but we still support it
			 * http://www.w3.org/TR/2013/CR-css3-values-20130730/
			 */
			return DeclarationValueFactory.getDeclarationValue(propertyName, "attr(" + value.getStringValue() + ")", ValueType.ATTR);
		case LexicalUnit.SAC_IDENT:
			/*
			 * Different types of values may be in this group, like
			 * color values, etc.
			 */
			return DeclarationValueFactory.getDeclarationValue(propertyName, value.getStringValue(), ValueType.IDENT);
			
		case LexicalUnit.SAC_STRING_VALUE:
			// Values coming between ' and " are modeled in this way in SAC
			return DeclarationValueFactory.getDeclarationValue(propertyName, "'" + value.getStringValue() + "'", ValueType.STRING);
			
		// All color could be converted to RGBA
		case LexicalUnitImpl.HEX_COLOR:
			// Three or six-digit hex value
			String realVal = "#" + value.getStringValue().toLowerCase();
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.COLOR);
		case LexicalUnit.SAC_RGBCOLOR:
			return getColorValue(propertyName, "rgb", value);
		case LexicalUnitImpl.RGBA_COLOR:
			return getColorValue(propertyName, "rgba", value);
		case LexicalUnitImpl.HSL_COLOR:
			return getColorValue(propertyName, "hsl", value);
		case LexicalUnitImpl.HSLA_COLOR:
			return getColorValue(propertyName, "hsla", value);
			
		case LexicalUnit.SAC_INTEGER:
			return DeclarationValueFactory.getDeclarationValue(propertyName, String.valueOf(value.getIntegerValue()), ValueType.INTEGER);
		case LexicalUnit.SAC_REAL:
			return DeclarationValueFactory.getDeclarationValue(propertyName, DeclarationValueFactory.formatFloat(value.getFloatValue()), ValueType.REAL);
			
		// Length values may be convertible to each other
		case LexicalUnit.SAC_PICA:
			return DeclarationValueFactory.getFontValue(propertyName, value.getFloatValue(), "pc");
		case LexicalUnit.SAC_POINT:
			return DeclarationValueFactory.getFontValue(propertyName, value.getFloatValue(), "pt");
		case LexicalUnit.SAC_INCH:
			return DeclarationValueFactory.getFontValue(propertyName, value.getFloatValue(), "in");
		case LexicalUnit.SAC_CENTIMETER:
			return DeclarationValueFactory.getFontValue(propertyName, value.getFloatValue(), "cm");
		case LexicalUnit.SAC_MILLIMETER:
			return DeclarationValueFactory.getFontValue(propertyName, value.getFloatValue(), "mm");
		case LexicalUnit.SAC_PIXEL:
			return DeclarationValueFactory.getFontValue(propertyName, value.getFloatValue(), "px");
			
		// We convert all angle values to degree.
		case LexicalUnit.SAC_GRADIAN:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "grad";
			return  DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.ANGLE);
		case LexicalUnit.SAC_RADIAN:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "rad";
			return  DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.ANGLE);
		case LexicalUnitImpl.TURN:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "turn";
			return  DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.ANGLE);
		case LexicalUnit.SAC_DEGREE:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "deg";
			return  DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.ANGLE);
			
		case LexicalUnit.SAC_KILOHERTZ:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "khz";
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.FREQUENCY);
		case LexicalUnit.SAC_HERTZ:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "hz";
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.FREQUENCY);
			
		// s and ms are convertible to each other
		case LexicalUnit.SAC_SECOND:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "s";
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.TIME);
		case LexicalUnit.SAC_MILLISECOND:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "ms";
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.TIME);
			
		// EM and % are somehow the same. 	
		case LexicalUnit.SAC_EM:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "em";			
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.LENGTH);
			
		case LexicalUnitImpl.REM:
			// 1rem = 100% of the parent's font. SO we don't add 
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "rem";
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.PERCENTAGE);
			
		case LexicalUnit.SAC_PERCENTAGE:
			realVal = DeclarationValueFactory.formatFloat(value.getFloatValue()) + "%";
			return DeclarationValueFactory.getDeclarationValue(propertyName, realVal, ValueType.PERCENTAGE);
		
			
		case LexicalUnit.SAC_EX:
			/*
			 *  EX is calculated in a different way across different browsers
			 * (IE-not known in which version-: 1ex = 0.5em, while not other browsers)
			 */
			return DeclarationValueFactory.getDeclarationValue(propertyName, DeclarationValueFactory.formatFloat(value.getFloatValue()) + "ex", ValueType.LENGTH);
		case LexicalUnit.SAC_DIMENSION:
			//Unknown dimension :)
			return DeclarationValueFactory.getDeclarationValue(propertyName, 
					DeclarationValueFactory.formatFloat(value.getFloatValue()) + value.getDimensionUnitText().toLowerCase(), ValueType.DIMENSION);
			
		case LexicalUnit.SAC_URI:
			realVal = "url('" + value.getStringValue() + "')";
			return DeclarationValueFactory.getDeclarationValue(propertyName,realVal, ValueType.URL);
			
		case LexicalUnit.SAC_OPERATOR_COMMA:
			return DeclarationValueFactory.getDeclarationValue(propertyName,",", ValueType.SEPARATOR);
			
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
		case LexicalUnit.SAC_FUNCTION:
			return DeclarationValueFactory.getDeclarationValue(propertyName, value.getFunctionName() + "(" + addSeparators(getAllValues(value.getFunctionName(), value.getParameters()), " ")	+ ")", ValueType.FUNCTION);
		case LexicalUnit.SAC_INHERIT:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"inherit", ValueType.INHERIT);
		case LexicalUnit.SAC_OPERATOR_EXP:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"^", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_GE:
			return DeclarationValueFactory.getDeclarationValue(propertyName,">=", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_GT:
			return DeclarationValueFactory.getDeclarationValue(propertyName,">", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_LE:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"<=", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_LT:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"<", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_MINUS:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"-", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_MOD:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"%", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_MULTIPLY:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"*", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_PLUS:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"+", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_SLASH:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"/", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_TILDE:
			return DeclarationValueFactory.getDeclarationValue(propertyName,"~", ValueType.OPERATOR);
		case LexicalUnit.SAC_RECT_FUNCTION: {
			return DeclarationValueFactory.getDeclarationValue(propertyName, "rect(" + addSeparators(getAllValues("rect", value.getParameters()), " ") + ")", ValueType.FUNCTION);
		}
		case LexicalUnit.SAC_SUB_EXPRESSION:
			// Never happens cause ca.concordia.cssanalyser.parser does not support it?
		case LexicalUnit.SAC_UNICODERANGE:
			// Cannot be expressed in CSS2
			
		/* One of the old versions of CSS3 working drafts pointed to background: (10px 10px) which was dropped at some point.
		 * So I dropped it as well
		 */
		//case LexicalUnitImpl.PARAN: 
		//	return "(" + addSeparators(getAllValues(value.getParameters()), " ") + ")" ;
		}
		throw new RuntimeException("Unhandled LexicalUnit type " + value.getLexicalUnitType());
	}

	private DeclarationValue getColorValue(String propertyName, String colorFunction, LexicalUnit value) {
		try {
			String val = colorFunction + "(" + addSeparators(getAllValues(colorFunction, value.getParameters()), ", ") + ")";
			return DeclarationValueFactory.getDeclarationValue(propertyName, val, ValueType.COLOR);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Adds separators between a list of values and returns the string
	 * containing those values separated with those separators
	 * @param listOfStrings
	 * @param separator
	 * @return
	 */
	private String addSeparators(List<DeclarationValue> listOfStrings, String separator) {
		String toReturn = "";
		for (DeclarationValue dv : listOfStrings)
			if (!separator.trim().equals(dv.toString().trim()))
				toReturn += dv + separator;
		return toReturn.substring(0, toReturn.length() - separator.length());
	}

	/**
	 * Returns the number of visited selector in the stylesheet
	 * @return
	 */
	public int getNumberOfVisitedSelectors() {
		return numberOfVisitedElements;
	}


	

}