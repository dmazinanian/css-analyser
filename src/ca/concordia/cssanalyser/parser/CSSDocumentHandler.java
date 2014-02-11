package ca.concordia.cssanalyser.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import ca.concordia.cssanalyser.csshelper.ColorHelper;
import ca.concordia.cssanalyser.csshelper.NamedColors;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationEquivalentValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.media.SingleMedia;
import ca.concordia.cssanalyser.cssmodel.media.GroupedMedia;
import ca.concordia.cssanalyser.cssmodel.media.Media;
import ca.concordia.cssanalyser.cssmodel.selectors.SimpleSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.DescendantSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.ChildSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.AdjacentSiblingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.SiblingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.PseudoClass;
import ca.concordia.cssanalyser.cssmodel.selectors.PseudoElement;
import ca.concordia.cssanalyser.cssmodel.selectors.NegationPseudoClass;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorCondition;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorConditionType;



public class CSSDocumentHandler implements DocumentHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSSDocumentHandler.class);

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
		// the rules starting with @ which are ignorable
	}

	@Override
	public void importStyle(String path, SACMediaList forMedia, String arg2)
			throws CSSException {
		//According to CSS3 '@import' rules must occur before all rules other than '@charset' rule
		File file = new File(styleSheet.getFilePath());
		String parentFolder = file.getParent(); 
		CSSParser parser = new CSSParser();
		try {
			StyleSheet importedStyleSheet = parser.parseExternalCSS(parentFolder + "/" + path);
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
		if (currentMedia == null) {
			currentMedia = getMedia(mediaList);
		} else {
			GroupedMedia groupedMedia;
			if (currentMedia instanceof SingleMedia) {
				groupedMedia = new GroupedMedia();
				groupedMedia.addMedia((SingleMedia) currentMedia);
			} else {
				groupedMedia = (GroupedMedia)currentMedia;
			}
			Media tempMedia = getMedia(mediaList);
			if (tempMedia instanceof SingleMedia)
				groupedMedia.addMedia((SingleMedia)tempMedia);
			else 
				groupedMedia.addAllMedia((GroupedMedia)tempMedia);
			currentMedia = groupedMedia;
		}	
	}
	
	private Media getMedia(SACMediaList sacMedia) {
		Media media;
		if (sacMedia.getLength() == 1) {
			media = new SingleMedia(sacMedia.item(0));
		} else {
			GroupedMedia groupedMedia = new GroupedMedia();
			for (int i = 0; i < sacMedia.getLength(); i++)
				groupedMedia.addMedia(new SingleMedia(sacMedia.item(i)));
			media = groupedMedia;
		}
		return media;
	}

	@Override
	public void endMedia(SACMediaList mediaList) throws CSSException {
		if (currentMedia instanceof SingleMedia) {
			currentMedia = null;
		} else {
			GroupedMedia groupedMedia = (GroupedMedia)currentMedia;
			for (int i = 0; i < mediaList.getLength(); i++) {
				groupedMedia.removeMedia(mediaList.item(i));
			}
			if (groupedMedia.size() == 1)
				currentMedia = new SingleMedia(groupedMedia.getAtomicMedia(0).getMediaName());
			else if (groupedMedia.size() == 0)
				currentMedia = null;
		}
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
			GroupingSelector groupedSelectors = new GroupingSelector(locator.getLineNumber(), locator.getColumnNumber());
			for (int i = 0; i < list.getLength(); i++) {
				try {
					BaseSelector newAtomicSelector = SACSelectorToAtomicSelector(list.item(i));
					newAtomicSelector.setLineNumber(locator.getLineNumber());
					newAtomicSelector.setColumnNumber(locator.getColumnNumber());
					if (currentMedia != null)
						newAtomicSelector.setMedia(currentMedia);
					groupedSelectors.add(newAtomicSelector);
				} catch (Exception ex) {
					LOGGER.warn(ex.toString());
				}
			}
			s = groupedSelectors;
		} else {
			try {
				s = SACSelectorToAtomicSelector(list.item(0));
				s.setLineNumber(locator.getLineNumber());
				s.setColumnNumber(locator.getColumnNumber());
				if (currentMedia != null)
					s.setMedia(currentMedia);
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
			BaseSelector childAtomicSelector = SACSelectorToAtomicSelector(sacDescendantSelector.getSimpleSelector());
			DescendantSelector s = new DescendantSelector(parentAtomicSelector, childAtomicSelector);

			return s;

		} else if (selector instanceof ChildSelectorImpl) {
			/*
			 * In fact we have three different occasions wherein this happens: A
			 * > B :first-letter :first-line
			 */

			ChildSelectorImpl sacChildSelectorImpl = (ChildSelectorImpl) selector;

			BaseSelector parentAtomicSelector = SACSelectorToAtomicSelector(sacChildSelectorImpl.getAncestorSelector());
			BaseSelector childAtomicSelector = SACSelectorToAtomicSelector(sacChildSelectorImpl.getSimpleSelector());

			BaseSelector selectorToReturn;

			if (sacChildSelectorImpl.getSimpleSelector() instanceof PseudoElementSelectorImpl) {
				selectorToReturn = parentAtomicSelector;
				PseudoElementSelectorImpl pseudoClass = (PseudoElementSelectorImpl) sacChildSelectorImpl.getSimpleSelector();

				((SimpleSelector) selectorToReturn).addPseudoClass(new PseudoClass(pseudoClass.getLocalName()));

			} else {
				selectorToReturn = new ChildSelector(
						parentAtomicSelector, childAtomicSelector);
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
			BaseSelector childSelector = SACSelectorToAtomicSelector(sacDirectAdjacentSelector.getSiblingSelector());
			return new AdjacentSiblingSelector(parentSelector, childSelector);
			
		} else if (selector instanceof AdjacentSelector) {
			
			AdjacentSelector sacAdjacentSelector = (AdjacentSelector) selector;
			BaseSelector parentSelector = SACSelectorToAtomicSelector(sacAdjacentSelector.getSelector());
			BaseSelector childSelector = SACSelectorToAtomicSelector(sacAdjacentSelector.getSiblingSelector());
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
			return new DeclarationValue("attr(" + value.getStringValue() + ")", ValueType.ATTR);
		case LexicalUnit.SAC_IDENT:
			/*
			 * Different types of values may be in this group, like
			 * color values, etc.
			 */
			String colorEquivalent;
			String stringValue = value.getStringValue();
			if ("currentColor".equals(stringValue))
				colorEquivalent = "currentColor";
			else 
				colorEquivalent = NamedColors.getRGBAColor(stringValue.toLowerCase());
			if (colorEquivalent != null)
				return new DeclarationEquivalentValue(stringValue, colorEquivalent, ValueType.COLOR);
			
			switch (stringValue) {
			case "none":
				return new DeclarationEquivalentValue(stringValue, stringValue, ValueType.IDENT);
			case "left":
			case "top":
				switch (propertyName) {
				case "background-position":
				case "background":
				case "perspective-origin":
				case "transform-origin":
					return new DeclarationEquivalentValue(stringValue, "0.0px", ValueType.LENGTH);
				}
				break;
			case "right":
			case "bottom":
				switch (propertyName) {
				case "background-position":
				case "background":
				case "perspective-origin":
				case "transform-origin":
					return new DeclarationEquivalentValue(stringValue, "100.0%", ValueType.LENGTH);
				}
				break;
			case "center":
				switch (propertyName) {
				case "background-position":
				case "background":
				case "perspective-origin":
				case "transform-origin":
					return new DeclarationEquivalentValue(stringValue, "50.0%", ValueType.LENGTH);
				}
				break;
			case "bold":
				return new DeclarationEquivalentValue(stringValue, "700", ValueType.INTEGER);
			case "normal":
				if (propertyName.equals("font-weight"))
					return new DeclarationEquivalentValue(stringValue, "400", ValueType.INTEGER);
				// What should we do for font shorthand property?!
			}

			return new DeclarationValue(stringValue, ValueType.IDENT);
			
		case LexicalUnit.SAC_STRING_VALUE:
			// Values coming between ' and " are modeled in this way in SAC
			return new DeclarationValue("'" + value.getStringValue() + "'", ValueType.STRING);
			
		// All color could be converted to RGBA
		case LexicalUnitImpl.HEX_COLOR:
			// Three or six-digit hex value
			return new DeclarationEquivalentValue("#" + value.getStringValue().toLowerCase(), ColorHelper.RGBAFromHEX(value.getStringValue()), ValueType.COLOR);
		case LexicalUnit.SAC_RGBCOLOR:
			// flute models the commas as operators so no separator needed - from GWT
			String realVal = "rgb(" + addSeparators(getAllValues("rgb", value.getParameters()), ", ") + ")";
			String eqVal = ColorHelper.RGBAfromRGB(value.getParameters());
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.COLOR);
		case LexicalUnitImpl.RGBA_COLOR:
			realVal = "rgba(" + addSeparators(getAllValues("rgba", value.getParameters()), ", ") + ")";
			eqVal = ColorHelper.RGBA(value.getParameters());
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.COLOR);
		case LexicalUnitImpl.HSL_COLOR:
			realVal = "hsl(" + addSeparators(getAllValues("hsl", value.getParameters()), ", ") + ")";
			eqVal = ColorHelper.RGBAFromHSLA(value.getParameters());
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.COLOR);
		case LexicalUnitImpl.HSLA_COLOR:
			realVal = "hsla(" + addSeparators(getAllValues("hsla", value.getParameters()), ", ") + ")";
			eqVal = ColorHelper.RGBAFromHSLA(value.getParameters());
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.COLOR);
			
		case LexicalUnit.SAC_INTEGER:
			int val = value.getIntegerValue();
			if (val == 0)
				switch (propertyName) {
				case "margin":
				case "margin-left":
				case "margin-right":
				case "margin-top":
				case "margin-bottom":
				case "padding":
				case "padding-left":
				case "padding-right":
				case "padding-bottom":
				case "padding-top":
				case "top":
				case "left":
				case "bottom":
				case "right":
				case "height":
				case "width":
				case "max-height":
				case "max-width":
				case "min-height":
				case "min-width":
				case "background-position":
				case "background-size":
				case "background":
				case "border":				
				case "border-bottom":
				case "border-left":				
				case "border-right":				
				case "border-top":				
				case "outline":
				case "border-top-width":
				case "border-bottom-width":
				case "border-left-width":
				case "border-right-width":
				case "border-width":
				case "outline-width":
				case "border-radius":
				case "-webkit-border-radius":
				case "-moz-border-radius":
				case "border-bottom-left-radius":
				case "border-bottom-right-radius":
				case "border-bottom-top-radius":
				case "border-bottom-bottom-radius":
				case "column-width":
				case "-webkit-column-width":
				case "-moz-column-width":
				case "column-rule-width":
				case "-moz-column-rule-width":
				case "-webkit-column-rule-width":
				case "column-gap":
				case "-moz-column-gap":
				case "-webkit-column-gap":
				case "perspective-origin":
				case "-webkit-perspective-origin":
				case "text-shadow":
				case "box-shadow":
				/*
				 * Dangorous to do!
				 * case "transform-origin":
				 * case "-ms-transform-origin":
				 * case "-webkit-transform-origin":
				 */
					
					return new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH);	
				}
			return new DeclarationValue(String.valueOf(val), ValueType.INTEGER);
		case LexicalUnit.SAC_REAL:
			return new DeclarationValue(String.valueOf(value.getFloatValue()), ValueType.REAL);
			
		// Length values may be convertible to each other
		case LexicalUnit.SAC_PICA:
			// 1pc = 12pt = 16px
			realVal = String.valueOf(value.getFloatValue()) + "pc";
			eqVal = String.valueOf(value.getFloatValue() * 16) + "px";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.LENGTH);
		case LexicalUnit.SAC_POINT:
			// 72pt is 96px
			realVal = String.valueOf(value.getFloatValue()) + "pt";
			eqVal = String.valueOf(value.getFloatValue() / 72F * 96F) + "px";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.LENGTH);
		case LexicalUnit.SAC_INCH:
			// Every inch is 96px
			realVal = String.valueOf(value.getFloatValue()) + "in";
			eqVal = String.valueOf(value.getFloatValue() * 96F) + "px";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.LENGTH);
		case LexicalUnit.SAC_CENTIMETER:
			// Every cm is (2.54^-1 * 96)px
			// In browser, every cm is about 38px
			realVal = String.valueOf(value.getFloatValue()) + "cm";
			eqVal = String.valueOf(value.getFloatValue() * 38F) + "px";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.LENGTH);
		case LexicalUnit.SAC_MILLIMETER:
			//every mm is 0.01 cm
			realVal = String.valueOf(value.getFloatValue()) + "mm";
			eqVal = String.valueOf(value.getFloatValue() * 38F / 100F) + "px";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.LENGTH);
		case LexicalUnit.SAC_PIXEL:
			realVal = eqVal = String.valueOf(value.getFloatValue()) + "px";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.LENGTH);
			
		// We convert all angle values to degree.
		case LexicalUnit.SAC_GRADIAN:
			// 1grad = 0.9deg
			realVal = String.valueOf(value.getFloatValue()) + "grad";
			eqVal = String.valueOf(value.getFloatValue() * 0.9F) + "deg";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.ANGLE);
		case LexicalUnit.SAC_RADIAN:
			// 2pi rad = 360deg
			realVal = String.valueOf(value.getFloatValue()) + "rad";
			eqVal = String.valueOf(value.getFloatValue() / (2 * 3.1415926F) * 360) + "deg";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.ANGLE);
		case LexicalUnitImpl.TURN:
			// 1turn = 360deg
			realVal = String.valueOf(value.getFloatValue()) + "turn";
			eqVal = String.valueOf(value.getFloatValue() * 360) + "deg";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.ANGLE);
		case LexicalUnit.SAC_DEGREE:
			realVal = eqVal = String.valueOf(value.getFloatValue()) + "deg";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.ANGLE);
			
		case LexicalUnit.SAC_KILOHERTZ:
			// 1KHz = 1000Hz
			realVal = String.valueOf(value.getFloatValue()) + "KHz";
			eqVal = String.valueOf(value.getFloatValue() * 1000) + "hz";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.FREQUENCY);
		case LexicalUnit.SAC_HERTZ:
			realVal = eqVal = value.getFloatValue() + "hz";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.FREQUENCY);
			
		// s and ms are convertible to each other
		case LexicalUnit.SAC_SECOND:
			// Each second is 1000 ms
			realVal = String.valueOf(value.getFloatValue()) + "s";
			eqVal = String.valueOf(value.getFloatValue() * 1000) + "ms";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.TIME);
		case LexicalUnit.SAC_MILLISECOND:
			realVal = eqVal = value.getFloatValue() + "ms";
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.TIME);
			
		// EM and % are somehow the same. 	
		case LexicalUnit.SAC_EM:
			// 1em = 100%, if we are talking about font
			realVal = String.valueOf(value.getFloatValue()) + "em";
			if ("font".equals(propertyName) || "font-size".equals(propertyName)) {
				eqVal = String.valueOf(value.getFloatValue() * 100) + "%";
				return new DeclarationEquivalentValue(realVal, eqVal, ValueType.PERCENTAGE);
			} 
			return new DeclarationValue(realVal, ValueType.TIME);
		case LexicalUnit.SAC_PERCENTAGE:
			realVal = eqVal = String.valueOf(value.getFloatValue() + "%");
			if (value.getFloatValue() == 0) {
				switch (propertyName) {
				case "background-position":
				case "background-size":
				case "border-radius":
				case "-webkit-border-radius":
				case "-moz-border-radius":
				case "border-bottom-left-radius":
				case "border-bottom-right-radius":
				case "border-bottom-top-radius":
				case "border-bottom-bottom-radius":
				case "transform-origin":
					eqVal = "0.0px";
					break;
				case "rgb":
				case "rgba":
				case "hsl":
				case "hsla":
					eqVal = "0";
				}
			}
			return new DeclarationEquivalentValue(realVal, eqVal, ValueType.PERCENTAGE);
		
			
		case LexicalUnit.SAC_EX:
			/*
			 *  EX is calculated in a different way across different browsers
			 * (IE-not known in which version-: 1ex = 0.5em, while not other browsers)
			 */
		case LexicalUnit.SAC_DIMENSION:
			//Unknown dimension :)
			return new DeclarationValue(String.valueOf(value.getFloatValue() + value.getDimensionUnitText().toLowerCase()), ValueType.DIMENSION);
			
		case LexicalUnit.SAC_URI:
			realVal = "url('" + value.getStringValue() + "')";
			if ("".equals(value.getStringValue().trim()))
				return new DeclarationEquivalentValue(realVal, "none", ValueType.URL);
			return new DeclarationValue(realVal, ValueType.URL);
		case LexicalUnit.SAC_OPERATOR_COMMA:
			return new DeclarationValue(",", ValueType.SEPARATOR);
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
		case LexicalUnit.SAC_FUNCTION:
			return new DeclarationValue(value.getFunctionName() + "(" + addSeparators(getAllValues(value.getFunctionName(), value.getParameters()), " ")	+ ")", ValueType.FUNCTION);
		case LexicalUnit.SAC_INHERIT:
			return new DeclarationValue("inherit", ValueType.INHERIT);
		case LexicalUnit.SAC_OPERATOR_EXP:
			return new DeclarationValue("^", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_GE:
			return new DeclarationValue(">=", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_GT:
			return new DeclarationValue(">", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_LE:
			return new DeclarationValue("<=", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_LT:
			return new DeclarationValue("<", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_MINUS:
			return new DeclarationValue("-", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_MOD:
			return new DeclarationValue("%", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_MULTIPLY:
			return new DeclarationValue("*", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_PLUS:
			return new DeclarationValue("+", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_SLASH:
			return new DeclarationValue("/", ValueType.OPERATOR);
		case LexicalUnit.SAC_OPERATOR_TILDE:
			return new DeclarationValue("~", ValueType.OPERATOR);
		case LexicalUnit.SAC_RECT_FUNCTION: {
			return new DeclarationValue( "rect(" + addSeparators(getAllValues("rect", value.getParameters()), " ") + ")", ValueType.FUNCTION);
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