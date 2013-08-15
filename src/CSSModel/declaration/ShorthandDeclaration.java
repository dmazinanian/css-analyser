/**
 * 
 */
package CSSModel.declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CSSModel.selectors.Selector;

/**
 * This class is another representation for shorthand properties.
 * A shorthand property is kept along with all its constituting 
 * properties. Default values are added as well. So, when comparing 
 * two declarations, we will have even more equal values,
 * cause margin: 2px would be equal to margin: 2px 2px and margin: 2px 2px 2px 2px
 *    
 * @author Davood Mazinanian
 *
 */
public class ShorthandDeclaration extends Declaration {

	private final Set<Declaration> constitutiveDeclarations;
	private static final Set<String> shorthandPropertiesNames = new HashSet<>();
	
	static {
		shorthandPropertiesNames.addAll(Arrays.asList(new String[] {
				
				"background", 
				"font",
				"list-style",
				"margin",
				"padding",
				"outline",
				"border",
				"border-color",
				"border-style",
				"border-width," +
				"border-image" +
				"border-top", "border-right", "border-bottom", "border-left",
				"transition"
				
			}
		));
	}
	
	/**
	 * @param property
	 * @param values
	 * @param belongsTo
	 * @param important
	 */
	public ShorthandDeclaration(String property, List<DeclarationValue> values,
			Selector belongsTo, boolean important) {
		this(property, values, belongsTo, -1, -1, important); 
	}

	/**
	 * @param property
	 * @param values
	 * @param belongsTo
	 * @param fileLineNumber
	 * @param fileColNumber
	 * @param important
	 */
	public ShorthandDeclaration(String property, List<DeclarationValue> values,
			Selector belongsTo, int fileLineNumber, int fileColNumber,
			boolean important) {
		super(property, values, belongsTo, fileLineNumber, fileColNumber,
				important);
		constitutiveDeclarations = new HashSet<>();
		//extractConstiutiveDeclarations();
	}

	@SuppressWarnings("unused")
	private void extractConstiutiveDeclarations() {
		switch (property) {

		case "background":
			
			/*
			 * CSS3 W3C:
			 * 	[ <bg-layer> , ]* <final-bg-layer>
					<bg-layer> = <bg-image> || <position> [ / <bg-size> ]? || <repeat-style> || <attachment> || <box>{1,2} 
					<final-bg-layer> = <bg-image> || <position> [ / <bg-size> ]? || <repeat-style> || <attachment> || <box>{1,2} || <'background-color'>
			 */
			
			/* Lets find the only "," allowed in the values
			 * This value is the splitter of different background layers 
			 */
			
			//background-color: 	Specifies the background color to be used				1
			// Background color could only become at the final-bg-layer so it is safe to look in all layers 
			DeclarationValue bgColorValue = searchVorValue(values, "rgba(.*)");
			if (bgColorValue == null) { // There is not an rgba in the converted values for this property 
				bgColorValue = new DeclarationValue("transparent");
			}
			Declaration bgColorDeclaration = new Declaration("background-color", bgColorValue , belongsTo, lineNumber, colNumber, isImportant);
			constitutiveDeclarations.add(bgColorDeclaration);

			// Try to separate the layers using comma
			DeclarationValue comma = new DeclarationValue(",");
			List<DeclarationValue> currentLayerValues = new ArrayList<>();
			List<DeclarationValue> bgImageValues = new ArrayList<>();
			List<DeclarationValue> bgSizeValues = new ArrayList<>();
			List<DeclarationValue> bgPositionValues = new ArrayList<>();
			List<DeclarationValue> bgRepatValues = new ArrayList<>();
			List<DeclarationValue> bgAttachementValues = new ArrayList<>();
			List<DeclarationValue> bgOriginValues = new ArrayList<>();
			
			int i = -1;
			while (++i < values.size()) {
				DeclarationValue currentValue = values.get(i);
				if (currentValue.equals(comma) || i == values.size() - 1) {
					
					DeclarationValue backgroundImage = searchVorValue(currentLayerValues, "url(.*)");
					if (backgroundImage != null)
						bgImageValues.add(backgroundImage);
					
					DeclarationValue backGroundSize = searchVorValue(currentLayerValues, "/");
					String width = "auto", height = "auto", round = "";
					if (backGroundSize != null) {
						String s = backGroundSize.getValue();
						s = s.substring(1, s.length() - 1);
						String[] parts = s.split(" ");
						
						if (parts.length == 1) {
							if (parts[0].equals("round")) { // (round) = (auto auto round)
								round = parts[0];
							} else { 						// (value) = (value value)
								width = height = parts[0];
							}
						} else if (parts.length == 2) {
							if (parts[0].equals("round")) { // (round value) = (value value round)
								round = parts[0]; 
								width = height = parts[1];
							} else if (parts[1].equals("round")) { // (value round) = (value value round) ???
								round = parts[1];
								width = height = parts[0];
							} else { // (value value) 
								width = parts[0];
								height = parts[1];
							}
						} else if(parts.length == 3) { // (value value round) 
							width = parts[0];
							height = parts[1];
							round = parts[2];
						}
					}
					
					String bgSizeString = width + " " + height;
					if (round.equals("round"))
						bgSizeString += " " + round;
					
					bgSizeValues.add(new DeclarationValue(bgSizeString));
					
						
					// Doing background-position
					String positionTop = "center";
					String positionLeft = "center";
					boolean posFound = false; 
					String declarationsString = getDeclarationValuesString(currentLayerValues); // Common searchForValue doesn't work here
					
					// Lets assume (almost?) all possible combinations for background position in the shorthand 
					Pattern pattern = Pattern.compile("(.*)(left|right|center) (top|bottom|center)(.*)");
					Matcher matcher = pattern.matcher(declarationsString);
					if (matcher.find()) {
						posFound = true;
						positionLeft = matcher.group(2);
						positionTop = matcher.group(3);
					}
					
					if (!posFound) {
						pattern = Pattern.compile("(.*)(top|bottom|center) (left|right|center)(.*)");
						matcher = pattern.matcher(declarationsString);
						if (matcher.find()) {
							posFound = true;
							positionLeft = matcher.group(3);
							positionTop = matcher.group(2);
						}
					}
					
					if (!posFound) {
						pattern = Pattern.compile("(.*?)(\\d*\\.?\\d+(%|px|em|ex|ch|rem|vw|vh|vm|cm|mm|in|pt|pc)) (\\d*\\.?\\d+(%|px|em|ex|ch|rem|vw|vh|vm|cm|mm|in|pt|pc))(.*?)");
						matcher = pattern.matcher(declarationsString);
						if (matcher.find()) {
							posFound = true;
							positionLeft = matcher.group(2);
							positionTop = matcher.group(4);
						}
					}
					
					if (!posFound) {
						pattern = Pattern.compile("(.*?)(\\d*\\.?\\d+(%|px|em|ex|ch|rem|vw|vh|vm|cm|mm|in|pt|pc)) (top|bottom|center|left|right)(.*?)");
						matcher = pattern.matcher(declarationsString);
						if (matcher.find()) {
							posFound = true;
							if (matcher.group(4).equals("left") || matcher.group(4).equals("right")) {
								positionLeft = matcher.group(4);
								positionTop = matcher.group(2);
							} else {
								positionLeft = matcher.group(2);
								positionTop = matcher.group(4);
							}
						}
					}
					
					if (!posFound) {
						pattern = Pattern.compile("(.*?)(top|bottom|center|left|right) (\\d+(%|px|em|ex|ch|rem|vw|vh|vm|cm|mm|in|pt|pc)) (.*?)");
						matcher = pattern.matcher(declarationsString);
						if (matcher.find()) {
							posFound = true;
							if (matcher.group(2).equals("top") || matcher.group(2).equals("bottom")) {
								positionLeft = matcher.group(4);
								positionTop = matcher.group(2);
							} else {
								positionLeft = matcher.group(2);
								positionTop = matcher.group(4);
							}
						}
					}
					
					if (!posFound) {
						pattern = Pattern.compile("(.*)(top|bottom|left|right)(.*)");
						matcher = pattern.matcher(declarationsString);
						if (matcher.find()) {
							posFound = true;
							if (matcher.group(2).equals("left|right")) {
								positionLeft = matcher.group(2);
							} else {
								positionTop = matcher.group(2);
							}
						}
					}
					
					if (!posFound) {
						pattern = Pattern.compile("(.*?)(\\d*\\.?\\d+(%|px|em|ex|ch|rem|vw|vh|vm|cm|mm|in|pt|pc))(.*?)");
						matcher = pattern.matcher(declarationsString);
						if (matcher.find()) {
							posFound = true;
							positionLeft = matcher.group(2);
						}
					}
					
					if (posFound) {
						bgPositionValues.add(new DeclarationValue(positionLeft + " " + positionTop));
					}
						
					
					// Now doing background repeat
					DeclarationValue backgroundRepeat = searchVorValue(currentLayerValues, "no-repeat|repeat-x|repeat-y");
					if (backgroundRepeat == null)
						backgroundRepeat = new DeclarationValue("repeat");
					
					bgRepatValues.add(backgroundRepeat);
					
					// Now doing background origin
					DeclarationValue backgroundOrigin = searchVorValue(currentLayerValues, "border-box|content-box");
					if (backgroundOrigin == null)
						backgroundOrigin = new DeclarationValue("padding-box");
					
					bgRepatValues.add(backgroundRepeat);
					
					// Now doing background attachment
					DeclarationValue backgroundAttachment = searchVorValue(currentLayerValues, "fixed|local");
					if (backgroundAttachment == null)
						backgroundAttachment = new DeclarationValue("scroll");
					
					bgAttachementValues.add(backgroundAttachment);
					
					currentLayerValues.clear();
					continue;
				}
				currentLayerValues.add(currentValue);
			}
			//background-image		Specifies ONE or MORE background images to be used		1 (3)
			// Add the Default value if missing
			if (bgImageValues.size() == 0)
				bgImageValues.add(new DeclarationValue("none"));
			Declaration bgImageDeclaration = new Declaration("background-image", bgImageValues, belongsTo, lineNumber, colNumber, isImportant, true);
			constitutiveDeclarations.add(bgImageDeclaration);
			
			//background-size		Specifies the size of the background images				3
			Declaration bgSizeDeclaration = new Declaration("background-size", bgSizeValues, belongsTo, lineNumber, colNumber, isImportant, true);
			constitutiveDeclarations.add(bgSizeDeclaration);
			
			//background-position	Specifies the position of the background images			1
			Declaration bgPositionDeclaration = new Declaration("background-position", bgPositionValues, belongsTo, lineNumber, colNumber, isImportant, true);
			constitutiveDeclarations.add(bgPositionDeclaration);
			
			//background-repeat		Specifies how to repeat the background images			1
			Declaration bgRepeatDeclaration = new Declaration("background-repeat", bgRepatValues, belongsTo, lineNumber, colNumber, isImportant, true);
			constitutiveDeclarations.add(bgRepeatDeclaration);
			
			//background-origin		Specifies the positioning area of the background images	3
			Declaration bgOriginDeclaration = new Declaration("background-origin", bgOriginValues, belongsTo, lineNumber, colNumber, isImportant, true);
			constitutiveDeclarations.add(bgOriginDeclaration);
			
			//background-clip		Specifies the painting area of the background images	3
			
			
			//background-attachment	Specifies whether the background images are fixed or
			//						scrolls with the rest of the page						1
			Declaration bgAttachmentDeclaration = new Declaration("background-attachment", bgAttachementValues, belongsTo, lineNumber, colNumber, isImportant, true);
			constitutiveDeclarations.add(bgAttachmentDeclaration);
			
			
			
			break;

		default:
			break;
		}
		
	}
	


	private DeclarationValue searchVorValue(List<DeclarationValue> vals, String regex) {
		//Pattern pattern = Pattern.compile(regex);
		for (DeclarationValue value : vals)
			if (Pattern.matches(regex, value.getValue()))
				return value;
		return null;
	}
	
	/*
	 * Searches the values for two or more exactly consecutive vlaues
	 
	private List<DeclarationValue> searchVorValues(List<DeclarationValue> vals, String... regexs) {
		List<DeclarationValue> toReturn = new ArrayList<>();
		for (int i = 0 ; i < vals.size(); i++) {
			if (Pattern.matches(regexs[0], vals.get(i).getValue())) {
				int j = 1; // Start from 1
				toReturn.add(vals.get(i));
				while (j < regexs.length && i + j < vals.size()) {
					if (Pattern.matches(regexs[j], vals.get(i + j).getValue()))
						toReturn.add(vals.get(i));
					else 
						break;
				}
				if (toReturn.size() == regexs.length) // We found the desired values
					return values;
				toReturn.clear();
			}
		}
		return null;
	}*/
	
	private String getDeclarationValuesString(List<DeclarationValue> vals) {
		StringBuilder s = new StringBuilder();
		for (DeclarationValue value : vals)
			s.append(value.getValue() + " ");
		s.delete(s.length() - 1, s.length());
		return s.toString();
	}
	
	public static boolean isShorthandProperty(String propertyName) {
		return shorthandPropertiesNames.contains(propertyName);
	}
	
	public String extendedString() {
		StringBuilder string = new StringBuilder();
		for (Declaration declaration : constitutiveDeclarations)
			string.append(declaration + "; \n");
		return string.toString();
	}

}
